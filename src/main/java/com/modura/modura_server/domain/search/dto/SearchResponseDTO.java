package com.modura.modura_server.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchContentListDTO {

        List<SearchContentDTO> contentList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchPlaceDTO {

        Long id;
        String name;
        Boolean isLiked;
        String thumbnail;
    }
}
