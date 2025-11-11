package com.modura.modura_server.domain.place.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
        private String comment;
        private List<String> imageUrl;
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