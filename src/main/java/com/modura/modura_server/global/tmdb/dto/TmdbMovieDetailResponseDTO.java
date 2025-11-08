package com.modura.modura_server.global.tmdb.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TmdbMovieDetailResponseDTO {

    private String title; // en-US로 호출한 영문 제목
    private Integer runtime;
}
