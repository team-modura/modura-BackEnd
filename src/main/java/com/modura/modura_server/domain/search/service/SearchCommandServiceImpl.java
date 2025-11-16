package com.modura.modura_server.domain.search.service;

import com.modura.modura_server.domain.content.entity.Category;
import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.ContentCategory;
import com.modura.modura_server.domain.content.repository.CategoryRepository;
import com.modura.modura_server.domain.content.repository.ContentCategoryRepository;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.global.tmdb.client.TmdbApiClient;
import com.modura.modura_server.global.tmdb.dto.TmdbMovieDetailResponseDTO;
import com.modura.modura_server.global.tmdb.dto.TmdbMovieResponseDTO;
import com.modura.modura_server.global.tmdb.dto.TmdbTVDetailResponseDTO;
import com.modura.modura_server.global.tmdb.dto.TmdbTVResponseDTO;
import com.modura.modura_server.global.tmdb.repository.TmdbBlacklistRepository;
import jakarta.annotation.PostConstruct;
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
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import scala.collection.Seq;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchCommandServiceImpl implements SearchCommandService {

    private final RedisTemplate<String, String> redisTemplate;

    private final TmdbApiClient tmdbApiClient;
    private final ContentRepository contentRepository;
    private final TmdbBlacklistRepository tmdbBlacklistRepository;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    private final CategoryRepository categoryRepository;
    private final ContentCategoryRepository contentCategoryRepository;
    private Map<Long, Category> categoryMap;

    private static final String POPULAR_KEYWORD_KEY = "popular:keywords";
    private static final int MIN_KEYWORD_LENGTH = 2;

    private static final String SEED_MOVIE_LOCK_KEY = "lock:seedMovie";
    private static final String SEED_TV_LOCK_KEY = "lock:seedTv";

    // 의미없는 문자 패턴 (자음/모음 단독, 특수문자 반복 등)
    private static final Pattern NOISE_PATTERN = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ]+$|[^가-힣a-zA-Z0-9\\s]+$");

    private static final int PAGES_TO_FETCH = 25; // 25 페이지 * 20개 = 500개
    private static final long API_THROTTLE_MS = 100;
    private static final String TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500";

    @PostConstruct
    public void initCategoryMap() {
        try {
            this.categoryMap = categoryRepository.findAll().stream()
                    .collect(Collectors.toMap(Category::getId, category -> category));
        } catch (Exception e) {
            log.error("Failed to initialize Category Map", e);
            this.categoryMap = Collections.emptyMap();
        }
    }

    private static class ContentWithCategories {
        final Content content;
        final List<Category> categories;

        ContentWithCategories(Content content, List<Category> categories) {
            this.content = content;
            this.categories = categories;
        }
    }

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
    public void seedMovie() {

        RLock lock = redissonClient.getLock(SEED_MOVIE_LOCK_KEY);
        boolean acquired = false;

        try {
            // 10초간 락 획득 시도, 락 획득 시 3분간 임대
            acquired = lock.tryLock(10, 180, TimeUnit.SECONDS);

            // 락 획득 실패 시
            if (!acquired) {
                log.warn("Movie seeding is already in progress.");
                return;
            }
            log.info("Acquired movie seeding lock. Starting manual seeding...");

            List<TmdbMovieResponseDTO.MovieResultDTO> movieDTOs = fetchPopularMovies();
            if (movieDTOs.isEmpty()) {
                log.warn("No movies fetched from TMDB discover API. Seeding job stopping.");
                return;
            }

            // API 응답 목록에서 ID 기준으로 중복 제거
            List<TmdbMovieResponseDTO.MovieResultDTO> uniqueMovieDTOs = new ArrayList<>(
                    movieDTOs.stream()
                            .collect(Collectors.toMap(
                                    TmdbMovieResponseDTO.MovieResultDTO::getId,
                                    dto -> dto,
                                    (existing, replacement) -> existing
                            ))
                            .values()
            );

            Set<Integer> incomingTmdbIds = extractMovieIds(uniqueMovieDTOs);
            Set<Integer> existingTmdbIds = fetchExistingContentIds(incomingTmdbIds);
            Set<Integer> blacklistedTmdbIds = fetchBlacklistedIds();

            List<TmdbMovieResponseDTO.MovieResultDTO> moviesToProcess = uniqueMovieDTOs.stream()
                    .filter(dto -> !existingTmdbIds.contains(dto.getId()))
                    .filter(dto -> !blacklistedTmdbIds.contains(dto.getId()))
                    .collect(Collectors.toList());

            if (moviesToProcess.isEmpty()) {
                log.info("Seeding job complete.");
                return;
            }

            List<ContentWithCategories> processedItems = buildMovieList(moviesToProcess);
            List<Content> newContentList = processedItems.stream()
                    .map(item -> item.content)
                    .collect(Collectors.toList());

            saveIfNotEmpty(newContentList, "movies");

            // ContentCategory 저장
            List<ContentCategory> newContentCategories = processedItems.stream()
                    .flatMap(item -> {
                        Content savedContent = item.content;
                        return item.categories.stream()
                                .map(category -> ContentCategory.builder()
                                        .content(savedContent)
                                        .category(category)
                                        .build());
                    })
                    .collect(Collectors.toList());

            if (!newContentCategories.isEmpty()) {
                log.info("Saving {} new content-category links for movies.", newContentCategories.size());
                transactionTemplate.executeWithoutResult(status -> {
                    contentCategoryRepository.saveAll(newContentCategories);
                });
            }
        } catch (InterruptedException e) {
            // 락을 기다리는 도중 인터럽트 발생 시
            Thread.currentThread().interrupt();
            log.warn("Movie seeding interrupted while waiting for lock.");
        } catch (Exception e) {
            // 시딩 로직 자체에서 예외 발생 시
            log.error("Error during manual movie seeding: {}", e.getMessage(), e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Async
    @Override
    public void seedSeries() {

        RLock lock = redissonClient.getLock(SEED_TV_LOCK_KEY);
        boolean acquired = false;

        try {
            // 10초간 락 획득 시도, 락 획득 시 3분간 임대
            acquired = lock.tryLock(10, 180, TimeUnit.SECONDS);

            // 락 획득 실패 시
            if (!acquired) {
                log.warn("Series seeding is already in progress.");
                return;
            }
            log.info("Acquired series seeding lock. Starting manual seeding...");

            List<TmdbTVResponseDTO.TVResultDTO> seriesDTOs = fetchPopularSeries();
            if (seriesDTOs.isEmpty()) {
                log.warn("No series fetched from TMDB discover API. Seeding job stopping.");
                return;
            }

            // API 응답 목록에서 ID 기준으로 중복 제거
            List<TmdbTVResponseDTO.TVResultDTO> uniqueMovieDTOs = new ArrayList<>(
                    seriesDTOs.stream()
                            .collect(Collectors.toMap(
                                    TmdbTVResponseDTO.TVResultDTO::getId,
                                    dto -> dto,
                                    (existing, replacement) -> existing
                            ))
                            .values()
            );

            Set<Integer> incomingTmdbIds = extractSeriesIds(uniqueMovieDTOs);
            Set<Integer> existingTmdbIds = fetchExistingContentIds(incomingTmdbIds);
            Set<Integer> blacklistedTmdbIds = fetchBlacklistedIds();

            List<TmdbTVResponseDTO.TVResultDTO> seriesToProcess = uniqueMovieDTOs.stream()
                    .filter(dto -> !existingTmdbIds.contains(dto.getId()))
                    .filter(dto -> !blacklistedTmdbIds.contains(dto.getId()))
                    .collect(Collectors.toList());

            if (seriesToProcess.isEmpty()) {
                log.info("Seeding job complete.");
                return;
            }

            List<ContentWithCategories> processedItems = buildSeriesList(seriesToProcess);
            List<Content> newContentList = processedItems.stream()
                    .map(item -> item.content)
                    .collect(Collectors.toList());

            saveIfNotEmpty(newContentList, "series");

            // ContentCategory 저장
            List<ContentCategory> newContentCategories = processedItems.stream()
                    .flatMap(item -> {
                        Content savedContent = item.content;
                        return item.categories.stream()
                                .map(category -> ContentCategory.builder()
                                        .content(savedContent)
                                        .category(category)
                                        .build());
                    })
                    .collect(Collectors.toList());

            if (!newContentCategories.isEmpty()) {
                log.info("Saving {} new content-category links for series.", newContentCategories.size());
                transactionTemplate.executeWithoutResult(status -> {
                    contentCategoryRepository.saveAll(newContentCategories);
                });
            }
        } catch (InterruptedException e) {
            // 락을 기다리는 도중 인터럽트 발생 시
            Thread.currentThread().interrupt();
            log.warn("Series seeding interrupted while waiting for lock.");
        } catch (Exception e) {
            // 시딩 로직 자체에서 예외 발생 시
            log.error("Error during manual series seeding: {}", e.getMessage(), e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public void saveContentsInTransaction(List<Content> contents) {
        transactionTemplate.executeWithoutResult(status -> {
            contentRepository.saveAll(contents);
        });
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

    private List<TmdbTVResponseDTO.TVResultDTO> fetchPopularSeries() {

        List<TmdbTVResponseDTO.TVResultDTO> allSeries = new ArrayList<>();
        for (int page = 1; page <= PAGES_TO_FETCH; page++) {
            try {
                log.debug("Fetching TMDB discover page: {}", page);
                TmdbTVResponseDTO response = tmdbApiClient.fetchPopularTVs(page).block();
                if (response != null && response.getResults() != null) {
                    allSeries.addAll(response.getResults());
                }
                Thread.sleep(API_THROTTLE_MS);
            } catch (Exception e) {
                log.warn("Failed to fetch page {} from TMDB. Skipping.", page, e);
            }
        }
        return allSeries;
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

    private Set<Integer> extractMovieIds(List<TmdbMovieResponseDTO.MovieResultDTO> movieDtos) {
        Set<Integer> ids = movieDtos.stream()
                .map(TmdbMovieResponseDTO.MovieResultDTO::getId)
                .collect(Collectors.toSet());
        log.info("Fetched {} unique movie IDs from TMDB.", ids.size());
        return ids;
    }

    private Set<Integer> extractSeriesIds(List<TmdbTVResponseDTO.TVResultDTO> seriesDtos) {
        Set<Integer> ids = seriesDtos.stream()
                .map(TmdbTVResponseDTO.TVResultDTO::getId)
                .collect(Collectors.toSet());
        log.info("Fetched {} unique series IDs from TMDB.", ids.size());
        return ids;
    }

    private Set<Integer> fetchExistingContentIds(Set<Integer> tmdbIds) {
        Set<Integer> existingIds = contentRepository.findAllByTmdbIdIn(tmdbIds)
                .stream().map(Content::getTmdbId)
                .collect(Collectors.toSet());
        log.info("Found {} existing content IDs in DB.", existingIds.size());
        return existingIds;
    }

    private Set<Integer> fetchBlacklistedIds() {
        Set<Integer> blacklist = tmdbBlacklistRepository.findAllTmdbIds();
        log.info("Found {} blacklisted content IDs.", blacklist.size());
        return blacklist;
    }

    private List<ContentWithCategories> buildMovieList(List<TmdbMovieResponseDTO.MovieResultDTO> moviesToProcess) {
        log.info("Fetching details for {} new movies...", moviesToProcess.size());
        return moviesToProcess.stream()
                .map(this::fetchDetailsAndBuildMovie)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<ContentWithCategories> fetchDetailsAndBuildMovie(TmdbMovieResponseDTO.MovieResultDTO listDto) {
        try {
            TmdbMovieDetailResponseDTO detailDto = tmdbApiClient.fetchMovieDetails(listDto.getId()).block();
            Thread.sleep(API_THROTTLE_MS);

            if (detailDto == null) {
                log.warn("Skipping movie. Failed to fetch details for new tmdbId: {}", listDto.getId());
                return Optional.empty();
            }

            Content content = Content.builder()
                    .titleKr(listDto.getTitle())
                    .titleEng(detailDto.getTitle())
                    .year(parseYearFromDate(listDto.getReleaseDate()))
                    .plot(listDto.getOverview())
                    .thumbnail(listDto.getPosterPath() != null ? TMDB_POSTER_BASE_URL + listDto.getPosterPath() : null)
                    .runtime(detailDto.getRuntime())
                    .type(2)
                    .tmdbId(listDto.getId())
                    .build();

            List<Category> categories = mapGenreIdsToCategories(listDto.getGenreIds());

            return Optional.of(new ContentWithCategories(content, categories));
        } catch (Exception e) {
            log.warn("Failed to process item for tmdbId {}: {}", listDto.getId(), e.getMessage());
            return Optional.empty();
        }
    }

    private List<ContentWithCategories> buildSeriesList(List<TmdbTVResponseDTO.TVResultDTO> seriesToProcess) {
        log.info("Fetching details for {} new series...", seriesToProcess.size());
        return seriesToProcess.stream()
                .map(this::fetchDetailsAndBuildSeries)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<ContentWithCategories> fetchDetailsAndBuildSeries(TmdbTVResponseDTO.TVResultDTO listDto) {
        try {
            TmdbTVDetailResponseDTO detailDto = tmdbApiClient.fetchTVDetails(listDto.getId()).block();
            Thread.sleep(API_THROTTLE_MS);

            if (detailDto == null) {
                log.warn("Skipping series. Failed to fetch details for new tmdbId: {}", listDto.getId());
                return Optional.empty();
            }

            Content content = Content.builder()
                    .titleKr(listDto.getName())
                    .titleEng(detailDto.getName())
                    .year(parseYearFromDate(listDto.getFirstAirDate()))
                    .plot(listDto.getOverview())
                    .thumbnail(listDto.getPosterPath() != null ? TMDB_POSTER_BASE_URL + listDto.getPosterPath() : null)
                    .type(1)
                    .tmdbId(listDto.getId())
                    .build();

            List<Category> categories = mapGenreIdsToCategories(listDto.getGenreIds());

            return Optional.of(new ContentWithCategories(content, categories));

        } catch (Exception e) {
            log.warn("Failed to process item for tmdbId {}: {}", listDto.getId(), e.getMessage());
            return Optional.empty();
        }
    }

    private List<Category> mapGenreIdsToCategories(List<Integer> genreIds) {

        if (genreIds == null || genreIds.isEmpty() || this.categoryMap == null) {
            return Collections.emptyList();
        }

        return genreIds.stream()
                .map(genreId -> this.categoryMap.get(Long.valueOf(genreId)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void saveIfNotEmpty(List<Content> contents, String contentType) {
        if (!contents.isEmpty()) {
            saveContentsInTransaction(contents);
            log.info("Successfully saved {} new contents to the database.", contents.size());
        } else {
            log.info("No contents were saved after fetching details.");
        }
    }
}