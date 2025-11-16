package com.modura.modura_server.global.tmdb.client;

import com.modura.modura_server.global.tmdb.dto.*;
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
     * [API 호출 1] 최신 영화 목록(Discover) 페이지 조회 (DB Seeding용)
     */
    public Mono<TmdbMovieResponseDTO> fetchNewestMovies(int page) {

        return webClient.get()
                .uri(TMDB_BASE_URL, uriBuilder -> uriBuilder
                        .path("/discover/movie")
                        .queryParam("include_adult", "false")
                        .queryParam("include_video", "false")
                        .queryParam("language", "ko-KR")
                        .queryParam("sort_by", "primary_release_date.desc")
                        .queryParam("with_original_language", "ko")
                        .queryParam("page", page)
                        .build())
                .header("Authorization", "Bearer " + TMDB_BEARER_TOKEN)
                .retrieve()
                .bodyToMono(TmdbMovieResponseDTO.class)
                .onErrorResume(e -> {
                    log.warn("Failed to fetch newest movies page {}: {}", page, e.getMessage());
                    return Mono.empty(); // discover 실패 시 해당 페이지 스킵
                });
    }

    /**
     * [API 호출 2] 인기 영화 목록(Discover) 페이지 조회 (인기 컨텐츠 캐싱용)
     */
    public Mono<TmdbMovieResponseDTO> fetchPopularMovies(int page) {

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
                    log.warn("Failed to fetch popular movies page {}: {}", page, e.getMessage());
                    return Mono.empty(); // discover 실패 시 해당 페이지 스킵
                });
    }

    /**
     * [API 호출 3] 영화 상세 정보 조회 (DB Seeding용)
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

    /**
     * [API 호출 4] 영화 Provider 정보 조회 (DB Seeding용)
     */
    public Mono<TmdbProviderResponseDTO> fetchMovieProviders(int tmdbId) {

        return webClient.get()
                .uri(TMDB_BASE_URL, uriBuilder -> uriBuilder
                        .path("/movie/{movie_id}/watch/providers")
                        .build(tmdbId))
                .header("Authorization", "Bearer " + TMDB_BEARER_TOKEN)
                .retrieve()
                .bodyToMono(TmdbProviderResponseDTO.class)
                .onErrorResume(e -> {
                    log.warn("Failed to fetch providers for movie tmdbId {}, skipping. Error: {}", tmdbId, e.getMessage());
                    return Mono.empty(); // 실패 시 빈 Mono 반환
                });
    }

    /**
     * [API 호출 1] 최신 TV 목록(Discover) 페이지 조회 (DB Seeding용)
     */
    public Mono<TmdbTVResponseDTO> fetchNewestTVs(int page) {

        return webClient.get()
                .uri(TMDB_BASE_URL, uriBuilder -> uriBuilder
                        .path("/discover/tv")
                        .queryParam("include_adult", "false")
                        .queryParam("include_video", "false")
                        .queryParam("language", "ko-KR")
                        .queryParam("sort_by", "first_air_date.desc")
                        .queryParam("with_original_language", "ko")
                        .queryParam("page", page)
                        .build())
                .header("Authorization", "Bearer " + TMDB_BEARER_TOKEN)
                .retrieve()
                .bodyToMono(TmdbTVResponseDTO.class)
                .onErrorResume(e -> {
                    log.warn("Failed to fetch newest tvs page {}: {}", page, e.getMessage());
                    return Mono.empty(); // discover 실패 시 해당 페이지 스킵
                });
    }

    /**
     * [API 호출 2] 인기 TV 목록(Discover) 페이지 조회 (인기 컨텐츠 캐싱용)
     */
    public Mono<TmdbTVResponseDTO> fetchPopularTVs(int page) {

        return webClient.get()
                .uri(TMDB_BASE_URL, uriBuilder -> uriBuilder
                        .path("/discover/tv")
                        .queryParam("include_adult", "false")
                        .queryParam("language", "ko-KR")
                        .queryParam("sort_by", "popularity.desc")
                        .queryParam("with_original_language", "ko")
                        .queryParam("page", page)
                        .build())
                .header("Authorization", "Bearer " + TMDB_BEARER_TOKEN)
                .retrieve()
                .bodyToMono(TmdbTVResponseDTO.class)
                .onErrorResume(e -> {
                    log.warn("Failed to fetch popular tvs page {}: {}", page, e.getMessage());
                    return Mono.empty(); // discover 실패 시 해당 페이지 스킵
                });
    }

    /**
     * [API 호출 3] TV 상세 정보 조회 (DB Seeding용)
     */
    public Mono<TmdbTVDetailResponseDTO> fetchTVDetails(int tmdbId) {

        return webClient.get()
                .uri(TMDB_BASE_URL, uriBuilder -> uriBuilder
                        .path("/tv/{series_id}")
                        .queryParam("language", "en-US")
                        .build(tmdbId))
                .header("Authorization", "Bearer " + TMDB_BEARER_TOKEN)
                .retrieve()
                .bodyToMono(TmdbTVDetailResponseDTO.class)
                .onErrorResume(e -> {
                    log.warn("Failed to fetch details for tmdbId {}, skipping. Error: {}", tmdbId, e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * [API 호출 4] TV Provider 정보 조회 (DB Seeding용)
     */
    public Mono<TmdbProviderResponseDTO> fetchTVProviders(int tmdbId) {

        return webClient.get()
                .uri(TMDB_BASE_URL, uriBuilder -> uriBuilder
                        .path("/tv/{series_id}/watch/providers")
                        .build(tmdbId))
                .header("Authorization", "Bearer " + TMDB_BEARER_TOKEN)
                .retrieve()
                .bodyToMono(TmdbProviderResponseDTO.class)
                .onErrorResume(e -> {
                    log.warn("Failed to fetch providers for TV tmdbId {}, skipping. Error: {}", tmdbId, e.getMessage());
                    return Mono.empty(); // 실패 시 빈 Mono 반환
                });
    }
}
