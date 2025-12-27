package com.flow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.mapper.FileMapper;
import com.flow.model.entity.File;
import com.flow.config.oss.OssConfig;
import com.flow.config.oss.OssTemplate;
import com.flow.service.FileService;
import io.minio.ComposeSource;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    private final OssTemplate ossTemplate;
    private final OssConfig ossConfig;
    private final StringRedisTemplate redisTemplate;

    @Override
    public File upload(MultipartFile file) {
        String fileName = ossTemplate.uploadFile(file);

        File fileEntity = new File();
        fileEntity.setName(fileName);
        fileEntity.setOriginalName(file.getOriginalFilename());
        fileEntity.setSize(file.getSize());
        fileEntity.setType(file.getContentType());
        fileEntity.setBucket(ossConfig.getBucketName());
        fileEntity.setPath(fileName);

        this.save(fileEntity);
        return fileEntity;
    }

    @Override
    public String getPreviewUrl(Long id) {
        File file = this.getById(id);
        if (file == null) {
            throw new RuntimeException("File not found");
        }
        return ossTemplate.getPreviewUrl(file.getPath());
    }

    @Override
    public void removeFile(Long id) {
        File file = this.getById(id);
        if (file != null) {
            ossTemplate.removeFile(file.getPath());
            this.removeById(id);
        }
    }

    @Override
    public boolean checkChunk(String fileMd5, int chunkIndex) {
        String key = "upload:chunk:" + fileMd5;
        return Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(key, chunkIndex));
    }

    @Override
    @SneakyThrows
    public void uploadChunk(String fileMd5, int chunkIndex, MultipartFile file) {
        String bucketName = ossConfig.getBucketName();
        String objectName = "chunks/" + fileMd5 + "/" + chunkIndex;

        ossTemplate.putObject(bucketName, objectName, file.getInputStream(), file.getSize(), file.getContentType());

        // Update Redis status
        String key = "upload:chunk:" + fileMd5;
        redisTemplate.opsForValue().setBit(key, chunkIndex, true);
    }

    @Override
    @SneakyThrows
    public File mergeChunks(String fileMd5, String fileName, Long totalSize) {
        String bucketName = ossConfig.getBucketName();
        String mergedObjectName = "merged/" + fileName; // Or generate a UUID name
        String chunkPrefix = "chunks/" + fileMd5 + "/";

        // List all chunks
        Iterable<Result<Item>> results = ossTemplate.listObjects(bucketName, chunkPrefix, true);
        List<ComposeSource> sources = new ArrayList<>();

        // MinIO listObjects might not return in order, but usually it does for numbered
        // chunks if padded.
        // However, our chunks are just integers. 1, 10, 2... order might be issue.
        // But wait, we are composing. The order matters.
        // We should sort them by index.

        List<Item> items = new ArrayList<>();
        for (Result<Item> result : results) {
            items.add(result.get());
        }

        // Sort items by chunk index (extracted from object name)
        items.sort((o1, o2) -> {
            int idx1 = Integer.parseInt(o1.objectName().substring(o1.objectName().lastIndexOf('/') + 1));
            int idx2 = Integer.parseInt(o2.objectName().substring(o2.objectName().lastIndexOf('/') + 1));
            return Integer.compare(idx1, idx2);
        });

        for (Item item : items) {
            sources.add(ComposeSource.builder().bucket(bucketName).object(item.objectName()).build());
        }

        if (sources.isEmpty()) {
            throw new RuntimeException("No chunks found to merge");
        }

        ossTemplate.composeObject(bucketName, mergedObjectName, sources);

        // Clean up chunks (Async ideally, but sync for now)
        for (Item item : items) {
            ossTemplate.removeFile(item.objectName());
        }
        String key = "upload:chunk:" + fileMd5;
        redisTemplate.delete(key);

        // Save file record
        File fileEntity = new File();
        fileEntity.setName(mergedObjectName);
        fileEntity.setOriginalName(fileName);
        fileEntity.setSize(totalSize);
        // fileEntity.setType(contentType); // Content type is hard to guess without
        // file
        fileEntity.setBucket(bucketName);
        fileEntity.setPath(mergedObjectName);

        this.save(fileEntity);
        return fileEntity;
    }
}
