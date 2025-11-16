package com.modura.modura_server.global.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TmdbTVResponseDTO {

    private Integer page;
    private List<TVResultDTO> results;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("total_results")
    private Integer totalResults;

    @Getter
    @NoArgsConstructor
    public static class TVResultDTO {

        private Integer id;
        private String overview;
        private String name;

        @JsonProperty("poster_path")
        private String posterPath;

        @JsonProperty("first_air_date")
        private String firstAirDate;

        @JsonProperty("genre_ids")
        private List<Integer> genreIds;
    }
}
