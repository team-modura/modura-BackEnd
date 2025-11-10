package com.modura.modura_server.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    //  S3에 이미지 업로드용 Presigned URL 발급 (PUT 방식)
    public String generateUploadPresignedUrlWithKey(String key, String contentType) {
        // S3에 업로드할 파일 요청 정보
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        // Presigned URL 발급 요청
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                r -> r.putObjectRequest(objectRequest)
                        .signatureDuration(Duration.ofMinutes(5))
        );

        return presignedRequest.url().toString(); // 프론트가 이 URL로 업로드하게 됨
    }


    // S3에 있는 이미지 조회용 Presigned URL 발급 (GET 방식)
    public String generateViewPresignedUrl(String key) {
        // 조회 요청 정보 생성
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        // Presigned GET URL 발급
        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest =
                s3Presigner.presignGetObject(getObjectPresignRequest);

        return presignedGetObjectRequest.url().toString();
    }

    // S3에서 이미지 삭제
    public void deleteFile(String imageUrl) {
        String key = extractKeyFromUrl(imageUrl);

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    /**
     *  S3 URL에서 Key 추출
     * ex: https://bucket.s3.amazonaws.com/folder/abc.jpg → folder/abc.jpg
     */
    private String extractKeyFromUrl(String url) {
        try {
            int idx = url.indexOf(".amazonaws.com/");
            if (idx == -1) {
                throw new IllegalArgumentException("유효하지 않은 S3 URL입니다.");
            }
            String key = url.substring(idx + ".amazonaws.com/".length());

            if (key.startsWith(bucket + "/")) {
                key = key.substring((bucket.length() + 1));
        }
            return key;
        } catch (Exception e) {
            throw new IllegalArgumentException("S3 URL에서 Key를 추출하는 중 오류가 발생했습니다.", e);
        }
    }
}