package com.modura.modura_server.global.config;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    private S3Presigner presigner;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.credentials.access-key:}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:}")
    private String secretKey;

    /**
     * 로컬: AccessKey/SecretKey 사용
     * 서버(EC2, ECS 등): IAM Role 자동 인식
     */
    @Bean
    public S3Presigner s3Presigner() {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(region));

        if (!accessKey.isEmpty() && !secretKey.isEmpty()) {
            // 로컬 환경 (credentials 파일 기반 or 수동 입력)
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    )
            );
        } else {
            // 서버 환경 (IAM Role 자동 인식)
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        presigner = builder.build();
        return presigner;
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        (!accessKey.isBlank() && !secretKey.isBlank())
                                ? StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey))
                                : DefaultCredentialsProvider.create()
                )
                .build();
    }

    /**
     * 스프링 종료 시 Presigner 리소스 정리
     */
    @PreDestroy
    public void cleanup() {
        if (presigner != null) {
            presigner.close();
        }
    }
}
