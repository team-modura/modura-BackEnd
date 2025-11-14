package com.modura.modura_server.domain.place.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
public class PlaceRequestDTO {
    @Builder
    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class PostPlaceReviewDTO {
        @NotNull
        @Min(value = 1)
        @Max(value = 5)
        private Integer rating;

        @NotBlank(message = "리뷰 내용은 필수입니다.")
        private String comment;

        private List<String> imageUrl;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class PatchPlaceReviewDTO {
        @Min(value = 1)
        @Max(value = 5)
        private Integer rating;

        @Size(min = 1, message = "리뷰 내용은 최소 1자 이상이어야 합니다.")
        private String comment;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class ImageKeysDTO {
        @NotEmpty(message = "이미지 키 목록은 비어있을 수 없습니다.")
        private List<String> imageKeys;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class PostStillcutDTO {

        @NotBlank(message = "이미지 URL은 필수입니다.")
        private String imageUrl;

        @NotNull
        @Min(value = 0)
        @Max(value = 100)
        private Integer similarity;

        @NotNull
        @Min(value = 0)
        @Max(value = 100)
        private Integer angle;

        @NotNull
        @Min(value = 0)
        @Max(value = 100)
        private Integer clarity;

        @NotNull
        @Min(value = 0)
        @Max(value = 100)
        private Integer color;

        @NotNull
        @Min(value = 0)
        @Max(value = 100)
        private Integer palette;
    }
}
