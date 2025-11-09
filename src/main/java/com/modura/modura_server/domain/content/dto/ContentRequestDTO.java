package com.modura.modura_server.domain.content.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
}
