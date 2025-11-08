package com.modura.modura_server.global.tmdb.service;

import com.modura.modura_server.domain.content.entity.Content;
import com.modura.modura_server.domain.content.repository.ContentRepository;
import com.modura.modura_server.global.tmdb.dto.TmdbMovieResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

        // API 호출
        TmdbMovieResponseDTO response = webClient.get()
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
                .block(); // 동기식으로 응답을 기다림

        if (response == null || response.getResults() == null) {
            log.warn("No results found from TMDB for page: {}", page);
            return;
        }

        List<Content> contentList = response.getResults().stream()
                .map(this::transformToContent)
                .filter(Objects::nonNull) // titleKr(title)이 없는 경우 필터링
                .collect(Collectors.toList());

        contentRepository.saveAll(contentList);
        log.info("Successfully saved {} movies from page {}.", contentList.size(), page);
    }

    private Content transformToContent(TmdbMovieResponseDTO.MovieResultDTO dto) {
log.info("Transforming movie result: {}", dto);
        return Content.builder()
                .titleKr(dto.getTitle())
                .year(parseYearFromDate(dto.getReleaseDate()))
                .plot(dto.getOverview())
                .thumbnail(dto.getPosterPath() != null ? TMDB_POSTER_BASE_URL + dto.getPosterPath() : null)
                .type(1)
                .tmdbId(dto.getId())
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
