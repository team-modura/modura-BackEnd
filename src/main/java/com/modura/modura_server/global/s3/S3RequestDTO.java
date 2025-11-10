package com.modura.modura_server.global.s3;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class S3RequestDTO {
    @Builder
    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class PresignedUploadReqDTO {

        @NotBlank(message = "폴더명은 필수입니다.")
        private String folder;

        @NotEmpty
        private List<String> fileNames;

        @NotEmpty
        private List<String> contentTypes;
    }
}