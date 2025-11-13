package com.modura.modura_server.domain.place.dto;

import com.modura.modura_server.domain.content.dto.ContentResponseDTO;
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
    public static class GetPlaceReviewDTO {
        Long placeReviewId;
        Integer rating;
        String comment;
        List<String> imageUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetPlaceReviewListDTO {
        List<GetPlaceReviewDTO> placeReviewList;
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
}
