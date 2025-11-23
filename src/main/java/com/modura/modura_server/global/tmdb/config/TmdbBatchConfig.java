package com.modura.modura_server.global.tmdb.config;

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
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TmdbBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final TmdbApiClient tmdbApiClient;
    private final ContentRepository contentRepository;
    private final ContentCategoryRepository contentCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final PlatformRepository platformRepository;

    private static final int CHUNK_SIZE = 20; // 한 번에 처리(Write)할 항목 수
    private static final int TOTAL_PAGES_TO_FETCH = 5; // 가져올 총 페이지 수
    private static final long API_THROTTLE_MS = 100; // API 호출 간 딜레이
    private static final String TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500";

    private Map<Long, Category> categoryMap;

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

    private static class ProcessedContentData {

        final Content content;
        final List<Category> categories;
        final List<String> platformNames;

        ProcessedContentData(Content content, List<Category> categories, List<String> platformNames) {
            this.content = content;
            this.categories = categories;
            this.platformNames = platformNames;
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

    // 1. Job 정의
    @Bean
    public Job tmdbMovieSeedingJob() {
        return new JobBuilder("tmdbMovieSeedingJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // Job을 반복 실행할 수 있게 함
                .start(tmdbMovieSeedingStep()) // Step 실행
                .build();
    }

    // 2. Step 정의
    @Bean
    public Step tmdbMovieSeedingStep() {
        return new StepBuilder("tmdbMovieSeedingStep", jobRepository)
                .<TmdbMovieResponseDTO.MovieResultDTO, ProcessedContentData>chunk(CHUNK_SIZE, transactionManager)
                .reader(tmdbMovieItemReader())
                .processor(tmdbMovieItemProcessor())
                .writer(newMovieItemWriter())
                .build();
    }

    // 3. ItemReader 정의
    @Bean
    @StepScope
    public ItemReader<TmdbMovieResponseDTO.MovieResultDTO> tmdbMovieItemReader() {

        Queue<TmdbMovieResponseDTO.MovieResultDTO> allMovies = new LinkedList<>();

        for (int page = 1; page <= TOTAL_PAGES_TO_FETCH; page++) {
            try {
                log.info("Fetching TMDB newest movies page: {}", page);
                TmdbMovieResponseDTO response = tmdbApiClient.fetchNewestMovies(page).block();
                if (response != null && response.getResults() != null) {
                    allMovies.addAll(response.getResults());
                }
                Thread.sleep(API_THROTTLE_MS);
            } catch (Exception e) {
                log.warn("Failed to fetch newest movies page {} during reader initialization", page, e);
            }
        }

        // API 응답 목록에서 ID 기준으로 중복 제거
        List<TmdbMovieResponseDTO.MovieResultDTO> uniqueMovies = new ArrayList<>(
                allMovies.stream()
                        .collect(Collectors.toMap(
                                TmdbMovieResponseDTO.MovieResultDTO::getId,
                                dto -> dto,
                                (existing, replacement) -> existing
                        ))
                        .values()
        );

        log.info("Total {} movie items to process.", uniqueMovies.size());

        return new IteratorItemReader<>(uniqueMovies);
    }

    // 4. ItemProcessor 정의
    @Bean
    @StepScope
    public org.springframework.batch.item.ItemProcessor<TmdbMovieResponseDTO.MovieResultDTO, ProcessedContentData> tmdbMovieItemProcessor() {
        return listDto -> {
            
            if (contentRepository.findAllByTmdbIdIn(Collections.singleton(listDto.getId())).size() > 0) {
                log.debug("Skipping existing movie tmdbId: {}", listDto.getId());
                return null;
            }

            try {
                int tmdbId = listDto.getId();

                Mono<TmdbMovieDetailResponseDTO> detailMono = tmdbApiClient.fetchMovieDetails(tmdbId)
                        .onErrorResume(e -> Mono.empty());
                Mono<TmdbProviderResponseDTO> providerMono = tmdbApiClient.fetchMovieProviders(tmdbId)
                        .onErrorResume(e -> Mono.empty());

                // 두 API 호출이 모두 완료될 때까지 대기
                Tuple2<TmdbMovieDetailResponseDTO, TmdbProviderResponseDTO> responses =
                        Mono.zip(detailMono, providerMono).block();

                TmdbMovieDetailResponseDTO detailDto = (responses != null) ? responses.getT1() : null;
                TmdbProviderResponseDTO providerDto = (responses != null) ? responses.getT2() : null;

                if (detailDto == null) {
                    log.warn("Skipping transformation due to missing detail data for movie tmdbId: {}", tmdbId);
                    return null;
                }

                Content content = Content.builder()
                        .titleKr(listDto.getTitle())
                        .titleEng(detailDto.getTitle())
                        .year(parseYearFromDate(listDto.getReleaseDate()))
                        .plot(listDto.getOverview())
                        .thumbnail(listDto.getPosterPath() != null ? TMDB_POSTER_BASE_URL + listDto.getPosterPath() : null)
                        .runtime(detailDto.getRuntime())
                        .type(2)
                        .tmdbId(tmdbId)
                        .build();

                List<Category> categories = mapGenreIdsToCategories(listDto.getGenreIds());

                // Provider 정보 파싱
                List<String> platformNames = Optional.ofNullable(providerDto)
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

                return new ProcessedContentData(content, categories, platformNames);

            } catch (Exception e) {
                log.warn("Failed to process movie item for tmdbId {}: {}", listDto.getId(), e.getMessage(), e);
                return null;
            }
        };
    }

    // 5. ItemWriter 정의
    @Bean
    @StepScope
    public ItemWriter<ProcessedContentData> newMovieItemWriter() {

        return (Chunk<? extends ProcessedContentData> chunk) -> {

            List<ProcessedContentData> processedItems = new ArrayList<>(chunk.getItems());
            if (processedItems.isEmpty()) {
                log.info("No new movie items to save in this chunk.");
                return;
            }

            // Content 엔티티 일괄 저장
            List<Content> newContentList = processedItems.stream()
                    .map(item -> item.content)
                    .collect(Collectors.toList());
            log.info("Saving {} new movie items to DB.", newContentList.size());
            contentRepository.saveAll(newContentList);

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

            // ContentCategory 일괄 저장
            if (!newContentCategories.isEmpty()) {
                log.info("Saving {} new content-category links for movies.", newContentCategories.size());
                contentCategoryRepository.saveAll(newContentCategories);
            }

            // Platform 일괄 저장
            List<Platform> newPlatforms = processedItems.stream()
                    .flatMap(item -> {
                        Content savedContent = item.content;
                        return item.platformNames.stream()
                                .map(name -> Platform.builder()
                                        .content(savedContent)
                                        .name(name)
                                        .build());
                    })
                    .collect(Collectors.toList());

            if (!newPlatforms.isEmpty()) {
                log.info("Saving {} new platform links for movies.", newPlatforms.size());
                platformRepository.saveAll(newPlatforms);
            }
        };
    }

    // 1. Job 정의
    @Bean
    public Job tmdbTVSeedingJob() {
        return new JobBuilder("tmdbTVSeedingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(tmdbTVSeedingStep())
                .build();
    }

    // 2. Step 정의
    @Bean
    public Step tmdbTVSeedingStep() {
        return new StepBuilder("tmdbTVSeedingStep", jobRepository)
                .<TmdbTVResponseDTO.TVResultDTO, ProcessedContentData>chunk(CHUNK_SIZE, transactionManager)
                .reader(tmdbTVItemReader())
                .processor(tmdbTVItemProcessor())
                .writer(newTVItemWriter())
                .build();
    }

    // 3. ItemReader 정의
    @Bean
    @StepScope
    public ItemReader<TmdbTVResponseDTO.TVResultDTO> tmdbTVItemReader() {

        Queue<TmdbTVResponseDTO.TVResultDTO> allTVs = new LinkedList<>();
        for (int page = 1; page <= TOTAL_PAGES_TO_FETCH; page++) {

            try {
                log.info("Fetching TMDB newest TV series page: {}", page);
                TmdbTVResponseDTO response = tmdbApiClient.fetchNewestTVs(page).block();
                if (response != null && response.getResults() != null) {
                    allTVs.addAll(response.getResults());
                }
                Thread.sleep(API_THROTTLE_MS);
            } catch (Exception e) {
                log.warn("Failed to fetch newest TV series page {} during reader initialization", page, e);
            }
        }

        // API 응답 목록에서 ID 기준으로 중복 제거
        List<TmdbTVResponseDTO.TVResultDTO> uniqueTVs = new ArrayList<>(
                allTVs.stream()
                        .collect(Collectors.toMap(
                                TmdbTVResponseDTO.TVResultDTO::getId,
                                dto -> dto,
                                (existing, replacement) -> existing
                        ))
                        .values()
        );

        log.info("Total {} TV series items to process.", uniqueTVs.size());

        return new IteratorItemReader<>(uniqueTVs);
    }

    // 4. ItemProcessor 정의
    @Bean
    @StepScope
    public org.springframework.batch.item.ItemProcessor<TmdbTVResponseDTO.TVResultDTO, ProcessedContentData> tmdbTVItemProcessor() {
        
        return listDto -> {
            
            if (contentRepository.findAllByTmdbIdIn(Collections.singleton(listDto.getId())).size() > 0) {
                log.debug("Skipping existing TV tmdbId: {}", listDto.getId());
                return null;
            }

            try {
                int tmdbId = listDto.getId();

                
                Mono<TmdbTVDetailResponseDTO> detailMono = tmdbApiClient.fetchTVDetails(tmdbId)
                        .onErrorResume(e -> Mono.empty());
                Mono<TmdbProviderResponseDTO> providerMono = tmdbApiClient.fetchTVProviders(tmdbId)
                        .onErrorResume(e -> Mono.empty());

                Tuple2<TmdbTVDetailResponseDTO, TmdbProviderResponseDTO> responses =
                        Mono.zip(detailMono, providerMono).block();

                TmdbTVDetailResponseDTO detailDto = (responses != null) ? responses.getT1() : null;
                TmdbProviderResponseDTO providerDto = (responses != null) ? responses.getT2() : null;

                
                if (detailDto == null) {
                    log.warn("Skipping transformation due to missing detail data for TV tmdbId: {}", tmdbId);
                    return null;
                }

                Content content = Content.builder()
                        .titleKr(listDto.getName())
                        .titleEng(detailDto.getName())
                        .year(parseYearFromDate(listDto.getFirstAirDate()))
                        .plot(listDto.getOverview())
                        .thumbnail(listDto.getPosterPath() != null ? TMDB_POSTER_BASE_URL + listDto.getPosterPath() : null)
                        .type(1)
                        .tmdbId(tmdbId)
                        .build();

                List<Category> categories = mapGenreIdsToCategories(listDto.getGenreIds());

                List<String> platformNames = Optional.ofNullable(providerDto)
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

                return new ProcessedContentData(content, categories, platformNames);

            } catch (Exception e) {
                log.warn("Failed to process TV item for tmdbId {}: {}", listDto.getId(), e.getMessage(), e);
                return null;
            }
        };
    }
    
    // 5. ItemWriter 정의
    @Bean
    @StepScope
    public ItemWriter<ProcessedContentData> newTVItemWriter() {

        return (Chunk<? extends ProcessedContentData> chunk) -> {

            List<ProcessedContentData> processedItems = new ArrayList<>(chunk.getItems());
            if (processedItems.isEmpty()) {
                log.info("No new TV items to save in this chunk.");
                return;
            }

            // Content 엔티티 일괄 저장
            List<Content> newContentList = processedItems.stream()
                    .map(item -> item.content)
                    .collect(Collectors.toList());
            log.info("Saving {} new TV items to DB.", newContentList.size());
            contentRepository.saveAll(newContentList);

            // ContentCategory 일괄 저장
            List<ContentCategory> newContentCategories = processedItems.stream()
                    .flatMap(item -> {
                        Content savedContent = item.content; // ID가 할당된 Content
                        return item.categories.stream()
                                .map(category -> ContentCategory.builder()
                                        .content(savedContent)
                                        .category(category)
                                        .build());
                    })
                    .collect(Collectors.toList());

            if (!newContentCategories.isEmpty()) {
                log.info("Saving {} new content-category links for TV.", newContentCategories.size());
                contentCategoryRepository.saveAll(newContentCategories);
            }

            // Platform 일괄 저장
            List<Platform> newPlatforms = processedItems.stream()
                    .flatMap(item -> {
                        Content savedContent = item.content;
                        return item.platformNames.stream()
                                .map(name -> Platform.builder()
                                        .content(savedContent)
                                        .name(name)
                                        .build());
                    })
                    .collect(Collectors.toList());

            if (!newPlatforms.isEmpty()) {
                log.info("Saving {} new platform links for TV.", newPlatforms.size());
                platformRepository.saveAll(newPlatforms);
            }
        };
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
}