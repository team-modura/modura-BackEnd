package com.modura.modura_server.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SearchResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchContentDTO {

        Long id;
        String title;
        Boolean isLiked;
        String thumbnail;
    }
}
