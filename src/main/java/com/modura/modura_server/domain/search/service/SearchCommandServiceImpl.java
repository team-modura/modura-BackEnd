package com.modura.modura_server.domain.search.service;

import com.modura.modura_server.domain.content.entity.Category;
import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.entity.ContentCategory;
import com.modura.modura_server.domain.content.entity.Platform;
import com.modura.modura_server.domain.content.repository.CategoryRepository;
import com.modura.modura_server.domain.content.repository.ContentCategoryRepository;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.domain.content.repository.PlatformRepository;
import com.modura.modura_server.global.tmdb.client.TmdbApiClient;
import com.modura.modura_server.global.tmdb.dto.*;
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
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import scala.collection.Seq;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
    private final PlatformRepository platformRepository;

    private Map<Long, Category> categoryMap;

    private static final String POPULAR_KEYWORD_KEY = "popular:keywords";
    private static final int MIN_KEYWORD_LENGTH = 2;

    private static final String SEED_MOVIE_LOCK_KEY = "lock:seedMovie";
    private static final String SEED_TV_LOCK_KEY = "lock:seedTv";

    // 의미없는 문자 패턴 (자음/모음 단독, 특수문자 반복 등)
    private static final Pattern NOISE_PATTERN = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ]+$|[^가-힣a-zA-Z0-9\\s]+$");

    private static final int POPULAR_PAGES_TO_FETCH = 50; // 인기 컨텐츠 50페이지 (1000개)
    private static final int NEWEST_PAGES_TO_FETCH = 10;  // 최신 컨텐츠 10페이지 (200개)
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

    private record ContentWithCategories(Content content, List<Category> categories, List<String> platformNames) { }

    @Async
    @Override
    public void incrementSearchKeyword(String query) {

        if (!StringUtils.hasText(query)) return;

        String trimmedQuery = query.trim();

        try {
            // 노이즈 제거 및 원본 쿼리 점수 증가
            processOriginalQuery(trimmedQuery);

            // OKT를 사용한 형태소 분석
            processExtractedKeywords(trimmedQuery);
        } catch (Exception e) {
            log.error("Failed to increment popular keyword score in Redis for query: {}", trimmedQuery, e);
        }
    }

    private void processOriginalQuery(String query) {

        if (!query.contains(" ")) {
            return;
        }
        String cleanQuery = NOISE_PATTERN.matcher(query).replaceAll("").trim();
        if (StringUtils.hasText(cleanQuery) && cleanQuery.length() >= MIN_KEYWORD_LENGTH) {
            redisTemplate.opsForZSet().incrementScore(POPULAR_KEYWORD_KEY, cleanQuery, 1.0);
        }
    }

    private void processExtractedKeywords(String query) {

        List<String> keywords = extractKeywords(query);
        for (String keyword : keywords) {
            if (keyword.length() >= MIN_KEYWORD_LENGTH) {
                redisTemplate.opsForZSet().incrementScore(POPULAR_KEYWORD_KEY, keyword, 1.0);
            }
        }
    }

    @Async
    @Override
    public void seedPopularMovie() {

        executeSeedingJob(
                SEED_MOVIE_LOCK_KEY,
                "Movie",
                () -> fetchMovies(POPULAR_PAGES_TO_FETCH, tmdbApiClient::fetchPopularMovies),
                TmdbMovieResponseDTO.MovieResultDTO::getId,
                this::buildMovieList
        );
    }

    @Override
    public void seedNewestMovie() {

        executeSeedingJob(
                SEED_MOVIE_LOCK_KEY,
                "Movie",
                () -> fetchMovies(NEWEST_PAGES_TO_FETCH, tmdbApiClient::fetchNewestMovies),
                TmdbMovieResponseDTO.MovieResultDTO::getId,
                this::buildMovieList
        );
    }

    @Async
    @Override
    public void seedPopularSeries() {

        executeSeedingJob(
                SEED_TV_LOCK_KEY,
                "Series",
                () -> fetchSeries(POPULAR_PAGES_TO_FETCH, tmdbApiClient::fetchPopularTVs),
                TmdbTVResponseDTO.TVResultDTO::getId,
                this::buildSeriesList
        );
    }

    @Override
    public void seedNewestSeries() {

        executeSeedingJob(
                SEED_TV_LOCK_KEY,
                "Series",
                () -> fetchSeries(NEWEST_PAGES_TO_FETCH, tmdbApiClient::fetchNewestTVs),
                TmdbTVResponseDTO.TVResultDTO::getId,
                this::buildSeriesList
        );
    }

    private <T> void executeSeedingJob(String lockKey,
                                       String jobName,
                                       java.util.function.Supplier<List<T>> listFetcher,
                                       java.util.function.Function<T, Integer> idExtractor,
                                       java.util.function.Function<List<T>, Flux<ContentWithCategories>> detailProcessor) {

        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;

        try {
            // 10초간 락 획득 시도, 락 획득 시 3분간 임대
            acquired = lock.tryLock(10, 180, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("{} seeding is already in progress.", jobName);
                return;
            }
            log.info("Acquired {} seeding lock. Starting manual seeding...", jobName);

            // 1. 인기 목록 조회
            List<T> allItems = listFetcher.get();
            if (allItems.isEmpty()) {
                log.warn("No {} fetched from TMDB API.", jobName);
                return;
            }

            // 2. 중복 제거 및 DB/블랙리스트 필터링
            List<T> newItems = filterNewContents(allItems, idExtractor);
            if (newItems.isEmpty()) {
                log.info("No new {} to save.", jobName);
                return;
            }

            // 3. 상세 정보 조회 및 엔티티 변환
            detailProcessor.apply(newItems)
                    .buffer(20) // 20개씩 데이터를 모음 (Batch)
                    .publishOn(Schedulers.boundedElastic()) // DB 저장(Blocking I/O)은 별도 스레드 풀에서 실행
                    .doOnNext(batch -> saveContentBatch(batch, jobName)) // 배치 단위로 DB 저장
                    .blockLast();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("{} seeding interrupted.", jobName);
        } catch (Exception e) {
            log.error("Error during {} seeding: {}", jobName, e.getMessage(), e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private <T> List<T> filterNewContents(List<T> items, java.util.function.Function<T, Integer> idExtractor) {

        List<T> uniqueItems = new ArrayList<>(
                items.stream()
                        .collect(Collectors.toMap(
                                idExtractor,
                                item -> item,
                                (existing, replacement) -> existing
                        ))
                        .values()
        );

        Set<Integer> incomingIds = uniqueItems.stream()
                .map(idExtractor)
                .collect(Collectors.toSet());

        Set<Integer> existingIds = fetchExistingContentIds(incomingIds);
        Set<Integer> blacklistedIds = fetchBlacklistedIds();

        return uniqueItems.stream()
                .filter(item -> {
                    Integer id = idExtractor.apply(item);
                    return !existingIds.contains(id) && !blacklistedIds.contains(id);
                })
                .collect(Collectors.toList());
    }

    private void saveContentBatch(List<ContentWithCategories> processedData, String contentType) {

        if (processedData.isEmpty()) {
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {
            // Content 저장
            List<Content> contents = processedData.stream()
                    .map(item -> item.content)
                    .collect(Collectors.toList());
            contentRepository.saveAll(contents);

            List<ContentCategory> categories = new ArrayList<>();
            List<Platform> platforms = new ArrayList<>();

            // ContentCategory 저장
            for (ContentWithCategories item : processedData) {
                Content savedContent = item.content(); // ID가 할당된 Content

                if (item.categories() != null) {
                    for (Category category : item.categories()) {
                        categories.add(ContentCategory.builder()
                                .content(savedContent)
                                .category(category)
                                .build());
                    }
                }

                if (item.platformNames() != null) {
                    for (String platformName : item.platformNames()) {
                        platforms.add(Platform.builder()
                                .content(savedContent)
                                .name(platformName)
                                .build());
                    }
                }
            }

            if (!categories.isEmpty()) {
                contentCategoryRepository.saveAll(categories);
            }
            if (!platforms.isEmpty()) {
                platformRepository.saveAll(platforms);
            }
        });

        log.info("Successfully saved {} new {} items.", processedData.size(), contentType);
    }

    private List<TmdbMovieResponseDTO.MovieResultDTO> fetchMovies(int pages, Function<Integer, Mono<TmdbMovieResponseDTO>> apiFetcher) {

        List<TmdbMovieResponseDTO.MovieResultDTO> allMovies = new ArrayList<>();
        for (int page = 1; page <= pages; page++) {
            try {
                log.debug("Fetching TMDB movie page: {}", page);
                TmdbMovieResponseDTO response = apiFetcher.apply(page).block();
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

    private List<TmdbTVResponseDTO.TVResultDTO> fetchSeries(int pages, Function<Integer, Mono<TmdbTVResponseDTO>> apiFetcher) {

        List<TmdbTVResponseDTO.TVResultDTO> allSeries = new ArrayList<>();
        for (int page = 1; page <= pages; page++) {
            try {
                log.debug("Fetching TMDB series page: {}", page);
                TmdbTVResponseDTO response = apiFetcher.apply(page).block();
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

    private Flux<ContentWithCategories> buildMovieList(List<TmdbMovieResponseDTO.MovieResultDTO> moviesToProcess) {

        log.info("Fetching details for {} new movies...", moviesToProcess.size());

        return Flux.fromIterable(moviesToProcess)
                .flatMap(this::fetchDetailsAndBuildMovie, 10);
    }

    private Mono<ContentWithCategories> fetchDetailsAndBuildMovie(TmdbMovieResponseDTO.MovieResultDTO listDto) {

        return Mono.zip(
                tmdbApiClient.fetchMovieDetails(listDto.getId()).onErrorResume(e -> Mono.empty()),
                tmdbApiClient.fetchMovieProviders(listDto.getId()).onErrorResume(e -> Mono.empty())
        ).flatMap(tuple -> {
            TmdbMovieDetailResponseDTO detailDto = tuple.getT1();
            TmdbProviderResponseDTO providerDto = tuple.getT2();

            if (detailDto == null) {
                return Mono.empty();
            }

            // 데이터 변환 및 객체 생성 로직
            Content content = buildMovieContent(listDto, detailDto);
            List<Category> categories = mapGenreIdsToCategories(listDto.getGenreIds());
            List<String> platformNames = extractPlatformNames(providerDto);

            return Mono.just(new ContentWithCategories(content, categories, platformNames));
        });
    }

    private Content buildMovieContent(TmdbMovieResponseDTO.MovieResultDTO listDto, TmdbMovieDetailResponseDTO detailDto) {

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
    }

    private Flux<ContentWithCategories> buildSeriesList(List<TmdbTVResponseDTO.TVResultDTO> seriesToProcess) {

        log.info("Fetching details for {} new series...", seriesToProcess.size());
        return Flux.fromIterable(seriesToProcess)
                .flatMap(this::fetchDetailsAndBuildSeries, 10);
    }

    private Mono<ContentWithCategories> fetchDetailsAndBuildSeries(TmdbTVResponseDTO.TVResultDTO listDto) {

        return Mono.zip(
                tmdbApiClient.fetchTVDetails(listDto.getId()).onErrorResume(e -> Mono.empty()),
                tmdbApiClient.fetchTVProviders(listDto.getId()).onErrorResume(e -> Mono.empty())
        ).flatMap(tuple -> {
            TmdbTVDetailResponseDTO detailDto = tuple.getT1();
            TmdbProviderResponseDTO providerDto = tuple.getT2();

            if (detailDto == null) {
                return Mono.empty();
            }

            // 데이터 변환 및 객체 생성 로직
            Content content = buildTVContent(listDto, detailDto);
            List<Category> categories = mapGenreIdsToCategories(listDto.getGenreIds());
            List<String> platformNames = extractPlatformNames(providerDto);

            return Mono.just(new ContentWithCategories(content, categories, platformNames));
        });
    }

    private Content buildTVContent(TmdbTVResponseDTO.TVResultDTO listDto, TmdbTVDetailResponseDTO detailDto) {

        return Content.builder()
                .titleKr(listDto.getName())
                .titleEng(detailDto.getName())
                .year(parseYearFromDate(listDto.getFirstAirDate()))
                .plot(listDto.getOverview())
                .thumbnail(listDto.getPosterPath() != null ? TMDB_POSTER_BASE_URL + listDto.getPosterPath() : null)
                .type(1)
                .tmdbId(listDto.getId())
                .build();
    }

    private List<String> extractPlatformNames(TmdbProviderResponseDTO providerDto) {

        return Optional.ofNullable(providerDto)
                .map(TmdbProviderResponseDTO::getResults)
                .map(TmdbProviderResponseDTO.Results::getKR)
                .map(TmdbProviderResponseDTO.ProviderCountryDetails::getFlatrate)
                .orElse(Collections.emptyList())
                .stream()
                .map(TmdbProviderResponseDTO.ProviderInfo::getProviderName)
                .filter(Objects::nonNull)
                .filter(name -> !"Netflix Standard with Ads".equals(name))
                .distinct()
                .collect(Collectors.toList());
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
}