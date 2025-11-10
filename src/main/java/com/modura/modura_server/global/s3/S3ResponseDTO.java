package com.modura.modura_server.global.s3;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class S3ResponseDTO {
    @Getter
    @AllArgsConstructor
    public static class PresignedUrlResDTO {
        private String key;
        private String presignedUrl;
    }
}
