package com.modura.modura_server.global.s3;

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
        private String folder;
        private List<String> fileNames;
        private List<String> contentTypes;
    }
}