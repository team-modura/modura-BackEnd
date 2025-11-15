package com.modura.modura_server.domain.place.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class PlaceResponseDTO {


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetPlaceReviewListDTO {
        List<ReviewItemDTO> placeReviewList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetStillcutDTO {
        Long stillcutId;
        Long contentId;
        String title;
        String imageUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetStillcutListDTO {

        List<GetStillcutDTO> stillcutList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewItemDTO {
        Long placeReviewId;
        String username;
        Integer rating;
        String comment;
        List<String> imageUrl;
        String createdAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentItemDTO {
        Long contentId;
        String title;
        String thumbnail;
        Boolean isLiked;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetPlaceDetailDTO {
        Long placeId;
        String name;
        Double reviewAvg;
        Integer reviewCount;
        Float latitude;
        Float longitude;
        Boolean isLiked;
        String placeImageUrl;
        List<ContentItemDTO> contentList;
        List<ReviewItemDTO> reviews;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetPlaceDTO {

        Long id;
        String name;
        Boolean isLiked;
        String thumbnail;
        Double rating;
        Integer reviewCount;
        Float latitude;
        Float longitude;
        List<String> content;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetPlaceListDTO {

        List<GetPlaceDTO> placeList;
    }
}
