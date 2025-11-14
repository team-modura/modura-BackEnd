package com.modura.modura_server.domain.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchCommandServiceImpl implements SearchCommandService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String POPULAR_KEYWORD_KEY = "popular:keywords";
    private static final int MIN_KEYWORD_LENGTH = 2;

    // 의미없는 문자 패턴 (자음/모음 단독, 특수문자 반복 등)
    private static final Pattern NOISE_PATTERN = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ]+$|[^가-힣a-zA-Z0-9\\s]+$");


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
}