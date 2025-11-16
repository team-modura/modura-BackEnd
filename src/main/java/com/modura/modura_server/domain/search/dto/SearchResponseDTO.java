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

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchPlaceListDTO {

        List<SearchPlaceDTO> placeList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetPopularKeywordDTO {

        List<String> keywords;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetTopContentDTO {

        Long id;
        String title;
        Boolean isLiked;
        String thumbnail;
        List<String> platforms;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetTopContentListDTO {

        List<GetTopContentDTO> contentList;
    }
}
