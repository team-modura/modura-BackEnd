package com.modura.modura_server.domain.content.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Builder
public class ContentRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewReqDTO {
        @NotNull
        @Min(1) @Max(5)
        private Integer rating;
        @NotBlank
        private String comment;
    }

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReviewUpdateReqDTO {
        @Min(1) @Max(5)
        private Integer rating;
        private String comment;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class PostBlacklistDTO {

        @NotEmpty(message = "TMDB ID 목록은 비어있을 수 없습니다.")
        List<Integer> tmdbIds;
    }
}
