package com.modura.modura_server.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AdminRequestDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePlaceDTO {

        @NotBlank(message = "장소 이름은 필수입니다.")
        private String name;

        @NotNull(message = "위도는 필수입니다.")
        private Float latitude;

        @NotNull(message = "경도는 필수입니다.")
        private Float longitude;

        @NotBlank(message = "썸네일 이미지 URL은 필수입니다.")
        private String thumbnail;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateStillcutDTO {

        @NotNull(message = "Content ID는 필수입니다.")
        private Long contentId;

        @NotNull(message = "Place ID는 필수입니다.")
        private Long placeId;

        @NotBlank(message = "스틸컷 이미지 URL은 필수입니다.")
        private String imageUrl;
    }
}