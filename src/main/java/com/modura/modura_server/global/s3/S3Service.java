package com.modura.modura_server.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    //  S3에 이미지 업로드용 Presigned URL 발급 (PUT 방식)
    public List<S3ResponseDTO.PresignedUrlResDTO> generateUploadPresignedUrl(S3RequestDTO.PresignedUploadReqDTO req) {

        if (req.getFileNames().size() != req.getContentTypes().size()) {
            throw new IllegalArgumentException("fileNames와 contentTypes의 크기가 일치하지 않습니다");
        }

        List<S3ResponseDTO.PresignedUrlResDTO> resultList = new ArrayList<>();

        for (int i = 0; i < req.getFileNames().size(); i++) {
            S3ResponseDTO.PresignedUrlResDTO presignedUrlDTO = generateSingleUploadPresignedUrl(
                    req.getFolder(),
                    req.getFileNames().get(i),
                    req.getContentTypes().get(i)
            );

            resultList.add(presignedUrlDTO);
        }

        return resultList;
    }

    private S3ResponseDTO.PresignedUrlResDTO generateSingleUploadPresignedUrl(String folder, String originalFileName, String contentType) {

        // 고유한 파일 경로 생성
        String key = folder + "/" + UUID.randomUUID() + "-" + originalFileName;

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

        return new S3ResponseDTO.PresignedUrlResDTO(key, presignedRequest.url().toString());
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

    public List<String> generateViewPresignedUrls(List<String> keys) {
        return keys.stream()
                .map(this::generateViewPresignedUrl)
                .toList();
    }

    // S3에서 이미지 삭제
    public void deleteFile(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    public void deleteAll(List<String> key) {
        for (String imageKey : key) {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(imageKey)
                    .build());
        }
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

    public String generateThumbnailUrl(String s3Key) {

        if (StringUtils.hasText(s3Key)) {
            return generateViewPresignedUrl(s3Key);
        }
        return null;
    }
}