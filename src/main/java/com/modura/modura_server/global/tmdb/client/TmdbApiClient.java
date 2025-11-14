package com.modura.modura_server.global.tmdb.client;

import com.modura.modura_server.global.tmdb.dto.TmdbMovieDetailResponseDTO;
import com.modura.modura_server.global.tmdb.dto.TmdbMovieResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbApiClient {

    private final WebClient webClient;

    @Value("${tmdb.api.base-url}")
    private String TMDB_BASE_URL;

    @Value("${tmdb.api.bearer-token}")
    private String TMDB_BEARER_TOKEN;

    /**
     * [API 호출 1] 영화 목록(Discover) 페이지 조회
     */
    public Mono<TmdbMovieResponseDTO> fetchMovieDiscoverPage(int page) {

        return webClient.get()
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
                .onErrorResume(e -> {
                    log.warn("Failed to fetch discover page {}: {}", page, e.getMessage());
                    return Mono.empty(); // discover 실패 시 해당 페이지 스킵
                });
    }

    /**
     * [API 호출 2] 영화 상세 정보 조회
     */
    public Mono<TmdbMovieDetailResponseDTO> fetchMovieDetails(int tmdbId) {

        return webClient.get()
                .uri(TMDB_BASE_URL, uriBuilder -> uriBuilder
                        .path("/movie/{movie_id}")
                        .queryParam("language", "en-US")
                        .build(tmdbId))
                .header("Authorization", "Bearer " + TMDB_BEARER_TOKEN)
                .retrieve()
                .bodyToMono(TmdbMovieDetailResponseDTO.class)
                .onErrorResume(e -> {
                    log.warn("Failed to fetch details for tmdbId {}, skipping. Error: {}", tmdbId, e.getMessage());
                    return Mono.empty();
                });
    }
}
