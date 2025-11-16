package com.modura.modura_server.domain.search.service;

import com.modura.modura_server.domain.content.dto.PopularContentCacheDTO;
import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.repository.*;
import com.modura.modura_server.global.tmdb.client.TmdbApiClient;
import com.modura.modura_server.global.tmdb.dto.TmdbMovieResponseDTO;
import com.modura.modura_server.global.tmdb.dto.TmdbTVResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PopularContentService {

    private final TmdbApiClient tmdbApiClient;
    private final ContentRepository contentRepository;

    private final RedisTemplate<String, Object> redisCacheTemplate;

    private static final String MOVIE_CACHE_KEY = "popular:movie";
    private static final String TV_CACHE_KEY = "popular:tv";
    private static final Duration CACHE_DURATION = Duration.ofHours(1);

    private static final int TARGET_COUNT = 10;
    private static final int MAX_PAGES_TO_FETCH = 5;
    private static final int FETCH_BUFFER_MULTIPLIER = 3;

    public PopularContentService(TmdbApiClient tmdbApiClient,
                                 ContentRepository contentRepository,
                                 @Qualifier("redisCacheTemplate") RedisTemplate<String, Object> redisCacheTemplate) {
        this.tmdbApiClient = tmdbApiClient;
        this.contentRepository = contentRepository;
        this.redisCacheTemplate = redisCacheTemplate;
    }

    public List<PopularContentCacheDTO> getPopularMovie() {
        return getFromCache(MOVIE_CACHE_KEY);
    }

    public void refreshPopularMovies() {
        log.info("Starting to refresh popular movie cache...");

        // 1. TMDB에서 인기 영화 ID 목록 조회
        List<Integer> allTmdbIds = fetchPopularMovieTmdbIdsFromAPI();
        if (allTmdbIds.isEmpty()) {
            log.warn("Failed to fetch any popular movies from TMDB. Cache refresh aborted.");
            return;
        }

        // 2. DB에 존재하는 영화만 필터링 및 DTO 변환
        List<PopularContentCacheDTO> popularContents = filterAndTransformToCacheDTO(allTmdbIds);
        if (popularContents.isEmpty()) {
            log.warn("No popular contents found in our DB from the fetched TMDB list. Cache will not be updated.");
            return;
        }

        // 3. Redis에 캐시 저장
        saveToCache(MOVIE_CACHE_KEY, popularContents, "Movie");
    }

    private List<Integer> fetchPopularMovieTmdbIdsFromAPI() {

        List<Integer> allTmdbIds = new ArrayList<>();

        for (int page = 1; page <= MAX_PAGES_TO_FETCH; page++) {
            if (allTmdbIds.size() >= TARGET_COUNT * FETCH_BUFFER_MULTIPLIER) {
                break;
            }
            try {
                TmdbMovieResponseDTO response = tmdbApiClient.fetchPopularMovies(page)
                        .block(Duration.ofSeconds(10));

                if (response != null && response.getResults() != null) {
                    allTmdbIds.addAll(
                            response.getResults().stream()
                                    .map(TmdbMovieResponseDTO.MovieResultDTO::getId)
                                    .collect(Collectors.toList())
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to fetch popular movies page {}. Skipping. Error: {}", page, e.getMessage());
            }
        }
        return allTmdbIds;
    }

    public List<PopularContentCacheDTO> getPopularTVs() {
        return getFromCache(TV_CACHE_KEY);
    }

    public void refreshPopularTVs() {
        log.info("Starting to refresh popular TV cache...");

        // 1. TMDB에서 인기 영화 ID 목록 조회
        List<Integer> allTmdbIds = fetchPopularTVTmdbIdsFromAPI();
        if (allTmdbIds.isEmpty()) {
            log.warn("Failed to fetch any popular TVs from TMDB. Cache refresh aborted.");
            return;
        }

        // 2. DB에 존재하는 영화만 필터링 및 DTO 변환
        List<PopularContentCacheDTO> popularContents = filterAndTransformToCacheDTO(allTmdbIds);
        if (popularContents.isEmpty()) {
            log.warn("No popular contents found in our DB from the fetched TMDB list. Cache will not be updated.");
            return;
        }

        // 3. Redis에 캐시 저장
        saveToCache(TV_CACHE_KEY, popularContents, "TV");
    }

    private List<Integer> fetchPopularTVTmdbIdsFromAPI() {

        List<Integer> allTmdbIds = new ArrayList<>();

        for (int page = 1; page <= MAX_PAGES_TO_FETCH; page++) {
            if (allTmdbIds.size() >= TARGET_COUNT * FETCH_BUFFER_MULTIPLIER) {
                break;
            }
            try {
                TmdbTVResponseDTO response = tmdbApiClient.fetchPopularTVs(page)
                        .block(Duration.ofSeconds(10));

                if (response != null && response.getResults() != null) {
                    allTmdbIds.addAll(
                            response.getResults().stream()
                                    .map(TmdbTVResponseDTO.TVResultDTO::getId)
                                    .collect(Collectors.toList())
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to fetch popular TVs page {}. Skipping. Error: {}", page, e.getMessage());
            }
        }
        return allTmdbIds;
    }

    private List<PopularContentCacheDTO> getFromCache(String cacheKey) {

        try {
            List<PopularContentCacheDTO> cachedList = (List<PopularContentCacheDTO>) redisCacheTemplate.opsForValue().get(cacheKey);

            if (cachedList != null) {
                log.debug("Popular content cache hit for key: {}", cacheKey);
                return cachedList;
            }
        } catch (Exception e) {
            log.error("Failed to retrieve popular content from Redis cache (key: {})", cacheKey, e);
        }
        log.warn("Popular content cache miss for key: {}", cacheKey);
        return Collections.emptyList();
    }

    private List<PopularContentCacheDTO> filterAndTransformToCacheDTO(List<Integer> allTmdbIds) {

        List<Content> contentsFromDb = contentRepository.findAllByTmdbIdIn(allTmdbIds);

        Map<Integer, Content> contentMap = contentsFromDb.stream()
                .collect(Collectors.toMap(
                        Content::getTmdbId,
                        content -> content,
                        (existing, replacement) -> existing
                ));
        
        return allTmdbIds.stream()
                .map(contentMap::get)           // DB에 있는 Content 객체 찾기
                .filter(Objects::nonNull)       // DB에 없는 것(null) 제외
                .limit(TARGET_COUNT)            // 최종 10개로 제한
                .map(this::convertToCacheDTO)   // 캐시 DTO로 변환
                .collect(Collectors.toList());
    }

    private PopularContentCacheDTO convertToCacheDTO(Content content) {

        return PopularContentCacheDTO.builder()
                .id(content.getId())
                .titleKr(content.getTitleKr())
                .thumbnail(content.getThumbnail())
                .build();
    }

    private void saveToCache(String cacheKey, List<PopularContentCacheDTO> contents, String type) {
        try {
            redisCacheTemplate.opsForValue().set(cacheKey, contents, CACHE_DURATION);
            log.info("Popular {} content cache refreshed successfully. Total {} items.", type, contents.size());
        } catch (Exception e) {
            log.error("Failed to save popular {} content to Redis cache (key: {})", type, cacheKey, e);
        }
    }
}