package com.flow.oss;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class OssTemplate {

    private final MinioClient minioClient;
    private final OssConfig ossConfig;

    @SneakyThrows
    public void createBucket(String bucketName) {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    @SneakyThrows
    public String uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("File name cannot be null");
        }

        // Ensure bucket exists
        createBucket(ossConfig.getBucketName());

        String fileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));
        InputStream inputStream = file.getInputStream();
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(ossConfig.getBucketName())
                .object(fileName)
                .stream(inputStream, file.getSize(), -1)
                .contentType(file.getContentType())
                .build());
        return fileName;
    }

    @SneakyThrows
    public String getPreviewUrl(String fileName) {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(ossConfig.getBucketName())
                .object(fileName)
                .method(Method.GET)
                .expiry(7, TimeUnit.DAYS)
                .build());
    }

    @SneakyThrows
    public void removeFile(String fileName) {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(ossConfig.getBucketName())
                .object(fileName)
                .build());
    }

    @SneakyThrows
    public void composeObject(String bucketName, String objectName, java.util.List<ComposeSource> sources) {
        minioClient.composeObject(
                ComposeObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .sources(sources)
                        .build());
    }

    @SneakyThrows
    public void putObject(String bucketName, String objectName, InputStream stream, long size, String contentType) {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(stream, size, -1)
                .contentType(contentType)
                .build());
    }

    @SneakyThrows
    public Iterable<Result<Item>> listObjects(String bucketName, String prefix, boolean recursive) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .recursive(recursive)
                .build());
    }
}
