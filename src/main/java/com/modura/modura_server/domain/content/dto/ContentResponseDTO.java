package com.modura.modura_server.domain.content.dto;

import lombok.*;
import java.util.List;

@Getter
@Builder
public class ContentResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewItemDTO {
        private Long id;
        private String username;
        private Integer rating;
        private String comment;
        private String createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewListDTO {
        private List<ReviewItemDTO> reviews;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StillCutPlaceItemDTO {
        private Long id;
        private String name;
        private String thumbnail;
        private Boolean isLiked;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContentDetailDTO {
        private Long id;
        private Integer tmdbId;
        private Integer type;
        private String titleKr;
        private String titleEng;
        private Boolean isLiked;
        private Integer runtime;
        private Integer year;
        private List<String> contentCategories;
        private String plot;
        private String thumbnail;
        private List<String> platforms;
        private Double reviewAvg;
        private Integer fiveStarCount;
        private Integer fourStarCount;
        private Integer threeStarCount;
        private Integer twoStarCount;
        private Integer oneStarCount;
        private List<ReviewItemDTO> reviews;
        private List<StillCutPlaceItemDTO> places;
    }
}