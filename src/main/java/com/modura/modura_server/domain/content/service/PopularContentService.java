package com.modura.modura_server.domain.content.service;

import com.modura.modura_server.domain.content.dto.PopularContentCacheDTO;
import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.repository.*;
import com.modura.modura_server.global.tmdb.client.TmdbApiClient;
import com.modura.modura_server.global.tmdb.dto.TmdbMovieResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularContentService {

    private final TmdbApiClient tmdbApiClient;
    private final ContentRepository contentRepository;

    @Qualifier("redisCacheTemplate")
    private final RedisTemplate<String, Object> redisCacheTemplate;

    private static final String CACHE_KEY = "popular:content";
    private static final Duration CACHE_DURATION = Duration.ofHours(1);

    private static final int TARGET_COUNT = 10;
    private static final int MAX_PAGES_TO_FETCH = 5;
    private static final int FETCH_BUFFER_MULTIPLIER = 3;

    public List<PopularContentCacheDTO> getPopularContent() {
        try {
            List<PopularContentCacheDTO> cachedList = (List<PopularContentCacheDTO>) redisCacheTemplate.opsForValue().get(CACHE_KEY);

            if (cachedList != null) {
                log.debug("Popular content cache hit.");
                return cachedList;
            }
        } catch (Exception e) {
            log.error("Failed to retrieve popular content from Redis cache", e);
        }
        log.warn("Popular content cache miss.");
        return Collections.emptyList();
    }

    public void refreshPopularContent() {
        log.info("Starting to refresh popular content cache...");
        List<Integer> allTmdbIds = new ArrayList<>();

        for (int page = 1; page <= MAX_PAGES_TO_FETCH; page++) {
            try {
                if (allTmdbIds.size() >= TARGET_COUNT * FETCH_BUFFER_MULTIPLIER) {
                    break;
                }

                TmdbMovieResponseDTO response = tmdbApiClient.fetchMovieDiscoverPage(page).block();

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

        if (allTmdbIds.isEmpty()) {
            log.warn("Failed to fetch any popular movies from TMDB. Cache refresh aborted.");
            return;
        }

        List<Content> contentsFromDb = contentRepository.findAllByTmdbIdIn(allTmdbIds);
        Map<Integer, Content> contentMap = contentsFromDb.stream()
                .collect(Collectors.toMap(Content::getTmdbId, content -> content));

        // Content 엔티티 리스트를 -> PopularContentCacheDTO 리스트로 변환
        List<PopularContentCacheDTO> orderedExistingContents = allTmdbIds.stream()
                .map(contentMap::get)
                .filter(Objects::nonNull)
                .limit(TARGET_COUNT)
                .map(content -> PopularContentCacheDTO.builder() // 엔티티를 캐시 DTO로 변환
                        .id(content.getId())
                        .titleKr(content.getTitleKr())
                        .thumbnail(content.getThumbnail())
                        .build())
                .collect(Collectors.toList());

        // 4. Redis 캐시 교체
        try {
            redisCacheTemplate.opsForValue().set(CACHE_KEY, orderedExistingContents, CACHE_DURATION);
            log.info("Popular content cache refreshed successfully. Total {} items.", orderedExistingContents.size());
        } catch (Exception e) {
            log.error("Failed to save popular content to Redis cache", e);
        }
    }
}