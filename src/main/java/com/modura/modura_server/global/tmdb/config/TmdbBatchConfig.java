package com.modura.modura_server.global.tmdb.config;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.global.tmdb.client.TmdbApiClient;
import com.modura.modura_server.global.tmdb.dto.TmdbMovieDetailResponseDTO;
import com.modura.modura_server.global.tmdb.dto.TmdbMovieResponseDTO;
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

    private static final int CHUNK_SIZE = 20; // 한 번에 처리(Write)할 항목 수
    private static final int TOTAL_PAGES_TO_FETCH = 5; // 가져올 총 페이지 수
    private static final long API_THROTTLE_MS = 100; // API 호출 간 딜레이
    private static final String TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500";


    // 1. Job 정의
    @Bean
    public Job tmdbSeedingJob() {
        return new JobBuilder("tmdbSeedingJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // Job을 반복 실행할 수 있게 함
                .start(tmdbSeedingStep()) // Step 실행
                .build();
    }

    // 2. Step 정의
    @Bean
    public Step tmdbSeedingStep() {
        return new StepBuilder("tmdbSeedingStep", jobRepository)
                .<TmdbMovieResponseDTO.MovieResultDTO, TmdbMovieResponseDTO.MovieResultDTO>chunk(CHUNK_SIZE, transactionManager)
                .reader(tmdbMovieItemReader())
                .writer(newContentItemWriter())
                .build();
    }

    // 3. ItemReader 정의
    @Bean
    @StepScope
    public ItemReader<TmdbMovieResponseDTO.MovieResultDTO> tmdbMovieItemReader() {

        Queue<TmdbMovieResponseDTO.MovieResultDTO> movieQueue = new LinkedList<>();

        for (int page = 1; page <= TOTAL_PAGES_TO_FETCH; page++) {
            try {
                log.info("Fetching TMDB newest movies page: {}", page);
                TmdbMovieResponseDTO response = tmdbApiClient.fetchNewestMovies(page).block();
                if (response != null && response.getResults() != null) {
                    movieQueue.addAll(response.getResults());
                }
                Thread.sleep(API_THROTTLE_MS);
            } catch (Exception e) {
                log.warn("Failed to fetch newest movies page {} during reader initialization", page, e);
            }
        }
        log.info("Total {} movie items to process.", movieQueue.size());

        return new IteratorItemReader<>(movieQueue);
    }

    // 4. ItemWriter 정의
    @Bean
    @StepScope
    public ItemWriter<TmdbMovieResponseDTO.MovieResultDTO> newMovieItemWriter() {
        return (Chunk<? extends TmdbMovieResponseDTO.MovieResultDTO> chunk) -> {

            // 1. 현재 청크에 포함된 모든 tmdbId 조회
            Set<Integer> incomingTmdbIds = chunk.getItems().stream()
                    .map(TmdbMovieResponseDTO.MovieResultDTO::getId)
                    .collect(Collectors.toSet());

            // 2. DB 조회를 1번만 실행하여, 이미 존재하는 tmdbId 목록 조회 (성능 핵심)
            Set<Integer> existingTmdbIds = contentRepository.findAllByTmdbIdIn(incomingTmdbIds)
                    .stream()
                    .map(Content::getTmdbId)
                    .collect(Collectors.toSet());

            // 3. [필터링 -> API 호출 -> 변환] 작업을 Stream으로 처리
            List<Content> newContentList = chunk.getItems().stream()
                    .filter(listDto -> !existingTmdbIds.contains(listDto.getId()))
                    .map(this::fetchDetailsAndBuildContent)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            // 4. 새로운 영화가 있을 경우에만 DB에 일괄 저장
            if (!newContentList.isEmpty()) {
                log.info("Saving {} new movie items to DB.", newContentList.size());
                contentRepository.saveAll(newContentList);
            } else {
                log.info("No new movie items to save in this chunk.");
            }
        };
    }

    private Optional<Content> fetchDetailsAndBuildContent(TmdbMovieResponseDTO.MovieResultDTO listDto) {
        try {
            // 4-1. 상세 정보 API 호출
            TmdbMovieDetailResponseDTO detailDto = tmdbApiClient.fetchMovieDetails(listDto.getId()).block();
            Thread.sleep(API_THROTTLE_MS); // API Throttling

            if (detailDto == null) {
                log.warn("Skipping transformation due to missing detail data for new tmdbId: {}", listDto.getId());
                return Optional.empty();
            }

            // 4-3. Content 엔티티 빌드
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

            return Optional.of(content);
        } catch (Exception e) {
            log.warn("Failed to process item for tmdbId {}: {}", listDto.getId(), e.getMessage());
            return Optional.empty();
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
}