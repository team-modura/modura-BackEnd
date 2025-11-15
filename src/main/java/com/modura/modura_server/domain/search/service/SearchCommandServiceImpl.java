package com.modura.modura_server.domain.search.service;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.global.tmdb.client.TmdbApiClient;
import com.modura.modura_server.global.tmdb.dto.TmdbMovieDetailResponseDTO;
import com.modura.modura_server.global.tmdb.dto.TmdbMovieResponseDTO;
import com.modura.modura_server.global.tmdb.repository.TmdbBlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchCommandServiceImpl implements SearchCommandService {

    private final RedisTemplate<String, String> redisTemplate;

    private final TmdbApiClient tmdbApiClient;
    private final ContentRepository contentRepository;
    private final TmdbBlacklistRepository tmdbBlacklistRepository;
    private final RedissonClient redissonClient;

    private static final String POPULAR_KEYWORD_KEY = "popular:keywords";
    private static final int MIN_KEYWORD_LENGTH = 2;

    private static final String SEED_MOVIE_LOCK_KEY = "lock:seedMovie";

    // 의미없는 문자 패턴 (자음/모음 단독, 특수문자 반복 등)
    private static final Pattern NOISE_PATTERN = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ]+$|[^가-힣a-zA-Z0-9\\s]+$");

    private static final int PAGES_TO_FETCH = 25; // 25 페이지 * 20개 = 500개
    private static final long API_THROTTLE_MS = 100;
    private static final String TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500";

    @Async
    @Override
    public void incrementSearchKeyword(String query) {

        if (!StringUtils.hasText(query)) {
            return;
        }

        String trimmedQuery = query.trim();

        try {
            if (trimmedQuery.contains(" ")) {
                String queryToIncrement = NOISE_PATTERN.matcher(trimmedQuery)
                        .replaceAll("")
                        .trim();

                if (StringUtils.hasText(queryToIncrement) && queryToIncrement.length() >= MIN_KEYWORD_LENGTH) {
                    redisTemplate.opsForZSet().incrementScore(
                            POPULAR_KEYWORD_KEY,
                            queryToIncrement,
                            1.0
                    );
                }
            }

            // OKT를 사용한 형태소 분석
            List<String> keywords = extractKeywords(trimmedQuery);

            for (String keyword : keywords) {
                if (keyword.length() >= MIN_KEYWORD_LENGTH) {
                    redisTemplate.opsForZSet().incrementScore(
                            POPULAR_KEYWORD_KEY,
                            keyword,
                            1.0
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to increment popular keyword score in Redis for query: {}", trimmedQuery, e);
        }
    }

    @Async
    @Override
    @Transactional
    public void seedMovie() {

        RLock lock = redissonClient.getLock(SEED_MOVIE_LOCK_KEY);
        boolean acquired = false;

        try {
            // 10초간 락 획득 시도, 락 획득 시 60초간 임대
            acquired = lock.tryLock(10, 60, TimeUnit.SECONDS);

            // 락 획득 실패 시
            if (!acquired) {
                log.warn("Movie seeding is already in progress.");
                return;
            }

            log.info("Acquired movie seeding lock. Starting manual seeding...");

            List<TmdbMovieResponseDTO.MovieResultDTO> movieDtos = fetchPopularMovies();
            if (movieDtos.isEmpty()) {
                log.warn("No movies fetched from TMDB discover API. Seeding job stopping.");
                return;
            }

            Set<Integer> incomingTmdbIds = movieDtos.stream()
                    .map(TmdbMovieResponseDTO.MovieResultDTO::getId)
                    .collect(Collectors.toSet());
            log.info("Fetched {} unique movie IDs from TMDB.", incomingTmdbIds.size());

            Set<Integer> existingTmdbIds = contentRepository.findAllByTmdbIdIn(incomingTmdbIds)
                    .stream()
                    .map(Content::getTmdbId)
                    .collect(Collectors.toSet());
            log.info("Found {} existing movie IDs in DB.", existingTmdbIds.size());

            Set<Integer> blacklistedTmdbIds = tmdbBlacklistRepository.findAllTmdbIds();
            log.info("Found {} blacklisted movie IDs.", blacklistedTmdbIds.size());

            List<TmdbMovieResponseDTO.MovieResultDTO> moviesToProcess = movieDtos.stream()
                    .filter(dto -> !existingTmdbIds.contains(dto.getId()))
                    .filter(dto -> !blacklistedTmdbIds.contains(dto.getId()))
                    .collect(Collectors.toList());

            if (moviesToProcess.isEmpty()) {
                log.info("Seeding job complete.");
                return;
            }

            log.info("Fetching details for {} new movies...", moviesToProcess.size());

            List<Content> newContentList = moviesToProcess.stream()
                    .map(this::fetchDetailsAndBuildContent)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!newContentList.isEmpty()) {
                contentRepository.saveAll(newContentList);
                log.info("Successfully saved {} new movies to the database.", newContentList.size());
            } else {
                log.info("No movies were saved after fetching details.");
            }
        } catch (InterruptedException e) {
            // 락을 기다리는 도중 인터럽트 발생 시
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // 시딩 로직 자체에서 예외 발생 시
            log.error("Error during manual movie seeding: {}", e.getMessage(), e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private List<TmdbMovieResponseDTO.MovieResultDTO> fetchPopularMovies() {

        List<TmdbMovieResponseDTO.MovieResultDTO> allMovies = new ArrayList<>();
        for (int page = 1; page <= PAGES_TO_FETCH; page++) {
            try {
                log.debug("Fetching TMDB discover page: {}", page);
                TmdbMovieResponseDTO response = tmdbApiClient.fetchPopularMovies(page).block();
                if (response != null && response.getResults() != null) {
                    allMovies.addAll(response.getResults());
                }
                Thread.sleep(API_THROTTLE_MS);
            } catch (Exception e) {
                log.warn("Failed to fetch page {} from TMDB. Skipping.", page, e);
            }
        }
        return allMovies;
    }

    private Content fetchDetailsAndBuildContent(TmdbMovieResponseDTO.MovieResultDTO listDto) {
        try {
            TmdbMovieDetailResponseDTO detailDto = tmdbApiClient.fetchMovieDetails(listDto.getId()).block();
            Thread.sleep(API_THROTTLE_MS);

            if (detailDto == null) {
                log.warn("Skipping movie. Failed to fetch details for new tmdbId: {}", listDto.getId());
                return null;
            }

            return Content.builder()
                    .titleKr(listDto.getTitle())
                    .titleEng(detailDto.getTitle())
                    .year(parseYearFromDate(listDto.getReleaseDate()))
                    .plot(listDto.getOverview())
                    .thumbnail(listDto.getPosterPath() != null ? TMDB_POSTER_BASE_URL + listDto.getPosterPath() : null)
                    .runtime(detailDto.getRuntime())
                    .type(2)
                    .tmdbId(listDto.getId())
                    .build();
        } catch (Exception e) {
            log.warn("Failed to process item for tmdbId {}: {}", listDto.getId(), e.getMessage());
            return null;
        }
    }

    private Integer parseYearFromDate(String releaseDate) {
        if (releaseDate == null || releaseDate.isBlank() || releaseDate.length() < 4) {
            return null;
        }
        try {
            return Integer.parseInt(releaseDate.substring(0, 4));
        } catch (NumberFormatException e) {
            log.warn("Failed to parse year from release date: {}", releaseDate);
            return null;
        }
    }

    private List<String> extractKeywords(String text) {

        List<String> keywords = new ArrayList<>();

        try {
            // 1. 텍스트 정규화 (normalize)
            CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);

            // 2. 토큰화 (tokenize)
            Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);

            // 3. Scala Seq를 Java List로 변환하고 명사/고유명사만 추출
            scala.collection.Iterator<KoreanTokenizer.KoreanToken> iterator = tokens.iterator();
            while (iterator.hasNext()) {
                KoreanTokenizer.KoreanToken token = iterator.next();
                String pos = token.pos().toString();

                // 명사(Noun) 또는 고유명사(ProperNoun)만 추출
                if ("Noun".equals(pos) || "ProperNoun".equals(pos)) {
                    String keyword = token.text();
                    if (keyword.length() >= MIN_KEYWORD_LENGTH) {
                        keywords.add(keyword);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract keywords using OKT from text: {}", text, e);
        }

        return keywords;
    }
}