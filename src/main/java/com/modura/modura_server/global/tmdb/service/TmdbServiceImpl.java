package com.modura.modura_server.global.tmdb.service;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.global.tmdb.dto.TmdbMovieDetailResponseDTO;
import com.modura.modura_server.global.tmdb.dto.TmdbMovieResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbServiceImpl implements TmdbService {

    private final ContentRepository contentRepository;
    private final WebClient webClient;

    @Value("${tmdb.api.base-url}")
    private String TMDB_BASE_URL;

    @Value("${tmdb.api.bearer-token}")
    private String TMDB_BEARER_TOKEN;

    private static final String TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500";

    @Override
    @Transactional
    public void fetchAndSaveMovies(int page) {

        // 1. Discover API 호출
        Mono<TmdbMovieResponseDTO> responseMono = webClient.get()
                .uri(TMDB_BASE_URL, uriBuilder -> uriBuilder
                        .path("/discover/movie")
                        .queryParam("include_adult", "false")
                        .queryParam("include_video", "false")
                        .queryParam("language", "ko-KR")
                        .queryParam("sort_by", "popularity.desc")
                        .queryParam("with_original_language", "ko")
                        .queryParam("page", page)
                        .build())
                .header("Authorization", "Bearer " + TMDB_BEARER_TOKEN)
                .retrieve()
                .bodyToMono(TmdbMovieResponseDTO.class)
                .onErrorResume(e -> Mono.empty()); // discover 실패 시 해당 페이지 스킵

        // 2. Mono<Response> -> Flux<MovieResultDTO> (영화 목록 스트림)
        Flux<TmdbMovieResponseDTO.MovieResultDTO> movieResultFlux = responseMono
                .filter(response -> response != null && response.getResults() != null)
                .flatMapMany(response -> Flux.fromIterable(response.getResults()));

        // 3. Fan-Out (병렬 호출) & Fan-In (결과 취합)
        Mono<List<Content>> contentListMono = movieResultFlux
                .flatMap(listDto -> {
                    Mono<TmdbMovieDetailResponseDTO> detailMono = fetchMovieDetails(listDto.getId());

                    return Mono.zip(Mono.just(listDto), detailMono);
                })
                .map(tuple -> transformToContent(tuple.getT1(), tuple.getT2())) // (listDto, detailDto) -> Content
                .filter(Objects::nonNull) // 변환 실패 케이스 필터링
                .collectList();

        // 4. 최종 DB 저장 (blocking)
        List<Content> contentList = contentListMono.block();

        if (contentList == null || contentList.isEmpty()) {
            log.warn("No valid movie results to save for page: {}", page);
            return;
        }

        contentRepository.saveAll(contentList);
        log.info("Successfully saved {} movies from page {}.", contentList.size(), page);
    }

    private Mono<TmdbMovieDetailResponseDTO> fetchMovieDetails(int tmdbId) {

        return webClient.get()
                .uri(TMDB_BASE_URL, uriBuilder -> uriBuilder
                        .path("/movie/{movie_id}")
                        .queryParam("language", "en-US")
                        .build(tmdbId))
                .header("Authorization", "Bearer " + TMDB_BEARER_TOKEN)
                .retrieve()
                .bodyToMono(TmdbMovieDetailResponseDTO.class)
                .doOnError(e -> log.warn("Failed to fetch details for tmdbId {}: {}", tmdbId, e.getMessage()))
                .onErrorResume(e -> {
                    log.warn("Failed to fetch details for tmdbId {}, skipping.", tmdbId, e);
                    return Mono.empty();
                });
    }

    private Content transformToContent(TmdbMovieResponseDTO.MovieResultDTO dto) {

        return Content.builder()
                .titleKr(dto.getTitle())
                .year(parseYearFromDate(dto.getReleaseDate()))
                .plot(dto.getOverview())
                .thumbnail(dto.getPosterPath() != null ? TMDB_POSTER_BASE_URL + dto.getPosterPath() : null)
                .type(1)
                .tmdbId(dto.getId())
                .build();
    }

    private Content transformToContent(TmdbMovieResponseDTO.MovieResultDTO listDto, TmdbMovieDetailResponseDTO detailDto) {

        log.info("Transforming movie result: {}", listDto.getTitle());
        return Content.builder()
                .titleKr(listDto.getTitle())
                .titleEng(detailDto.getTitle())
                .year(parseYearFromDate(listDto.getReleaseDate()))
                .plot(listDto.getOverview())
                .thumbnail(listDto.getPosterPath() != null ? TMDB_POSTER_BASE_URL + listDto.getPosterPath() : null)
                .runtime(detailDto.getRuntime())
                .type(1)
                .tmdbId(listDto.getId())
                .build();
    }

    private Integer parseYearFromDate(String releaseDate) {

        if (releaseDate == null || releaseDate.isBlank() || releaseDate.length() < 4) {
            return null;
        }

        try {
            String yearString = releaseDate.substring(0, 4);
            return Integer.parseInt(yearString);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse year from release date: {}", releaseDate);
            return null;
        }
    }
}
