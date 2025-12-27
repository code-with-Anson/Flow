package com.flow.config.oss;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class OssConfig {

    private String endpoint;
    private String externalEndpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    public OssTemplate ossTemplate(MinioClient minioClient, OssConfig ossConfig) {
        MinioClient externalMinioClient = null;
        if (ossConfig.getExternalEndpoint() != null && !ossConfig.getExternalEndpoint().isEmpty()) {
            externalMinioClient = MinioClient.builder()
                    .endpoint(ossConfig.getExternalEndpoint())
                    .credentials(ossConfig.getAccessKey(), ossConfig.getSecretKey())
                    .build();
        }
        return new OssTemplate(minioClient, externalMinioClient, ossConfig);
    }
}
