package com.bookstore.content;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties properties;

    @Bean
    public MinioClient minioClient() throws Exception {
        MinioClient client = MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .build();

        boolean bucketExists = client.bucketExists(
                BucketExistsArgs.builder().bucket(properties.bucket()).build());

        if (!bucketExists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(properties.bucket()).build());
        }

        return client;
    }
}