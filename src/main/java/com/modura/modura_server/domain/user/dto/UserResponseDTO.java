package com.modura.modura_server.domain.user.dto;

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
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetContentReviewDTO {

        Long id;
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
