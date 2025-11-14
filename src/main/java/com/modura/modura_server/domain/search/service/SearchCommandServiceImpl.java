package com.modura.modura_server.domain.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchCommandServiceImpl implements SearchCommandService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String POPULAR_KEYWORD_KEY = "popular:keywords";

    @Async
    @Override
    public void incrementSearchKeyword(String query) {

        if (!StringUtils.hasText(query)) {
            return;
        }

        // (2) "의미있는 단위": 공백(whitespace) 기준으로 분리
        String[] keywords = query.trim().split("\\s+");

        try {
            for (String keyword : keywords) {
                // (3) 유효한 키워드(예: 2자 이상)만 집계
                if (keyword.length() >= 2) {
                    // Redis ZSET의 스코어 증가
                    redisTemplate.opsForZSet().incrementScore(
                            POPULAR_KEYWORD_KEY,
                            keyword,
                            1.0
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to increment popular keyword score in Redis", e);
        }
    }
}