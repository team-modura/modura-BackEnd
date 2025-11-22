package com.modura.modura_server.domain.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class UserResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetMyStillcutDTO {

        Long id;
        String imageUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetMyStillcutListDTO {

        List<GetMyStillcutDTO> stillcutList;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetMyStillcutDetailDTO {

        Long id;
        String imageUrl;
        String stillcut;
        String title;
        String name;
        String date;
        Integer similarity;
        Integer angle;
        Integer clarity;
        Integer color;
        Integer palette;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetContentReviewDTO {

        Long id;
        Long contentId;
        String title;
        String username;
        Integer rating;
        String comment;
        String createdAt;
        String thumbnail;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetPlaceReviewDTO {

        Long id;
        Long placeId;
        String name;
        String username;
        Integer rating;
        String comment;
        List<String> imageUrl;
        String createdAt;
        String thumbnail;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetReviewListDTO {

        List<GetContentReviewDTO> contentReviewList;
        List<GetPlaceReviewDTO> placeReviewList;
    }
}
