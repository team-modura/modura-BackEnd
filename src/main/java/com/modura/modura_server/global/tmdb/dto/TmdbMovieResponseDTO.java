package com.modura.modura_server.global.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TmdbMovieResponseDTO {
    private Integer page;
    private List<MovieResultDTO> results;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("total_results")
    private Integer totalResults;

    @Getter
    @NoArgsConstructor
    public static class MovieResultDTO {
        private Integer id;
        private String overview;
        private String title;

        @JsonProperty("poster_path")
        private String posterPath;

        @JsonProperty("release_date")
        private String releaseDate;
    }
}
