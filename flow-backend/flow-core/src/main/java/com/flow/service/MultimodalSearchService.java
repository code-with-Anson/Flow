package com.flow.service;

import com.flow.ai.service.MultimodalEmbeddingService;
import com.flow.model.entity.File;
import com.flow.model.es.MultimodalAsset;
import com.flow.oss.OssTemplate;
import com.flow.repository.es.MultimodalAssetRepository;
import com.flow.model.dto.FileProcessingMessage;
import com.flow.mq.RabbitMQProducer;
import com.flow.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.flow.common.enums.ErrorCode;
import com.flow.common.exception.BusinessException;

@Service
@Slf4j
@RequiredArgsConstructor
public class MultimodalSearchService {

    private final MultimodalEmbeddingService embeddingService;
    private final MultimodalAssetRepository multimodalAssetRepository;
    private final OssTemplate ossTemplate;
    private final FileService fileService;
    private final RabbitMQProducer rabbitMQProducer;

    /**
     * 初始化上传并排队等待处理
     */
    @SneakyThrows
    public com.flow.model.entity.File initiateUpload(MultipartFile file, String description, String userId,
            String model) {
        // 1. 上传到 MinIO 并保存初始记录
        com.flow.model.entity.File fileEntity = fileService.upload(file);

        // 2. 更新状态为 QUEUED (排队中)
        fileEntity.setStatus(File.STATUS_QUEUED);
        fileService.updateById(fileEntity);

        // 3. 提交处理
        submitForProcessing(fileEntity, description, userId, model);

        return fileEntity;
    }

    /**
     * 处理已上传的文件 (用于分片上传后的触发)
     */
    @SneakyThrows
    public void processUploadedFile(Long fileId, String description, String userId, String model) {
        com.flow.model.entity.File fileEntity = fileService.getById(fileId);
        if (fileEntity == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "文件不存在");
        }

        // 更新状态为 QUEUED
        fileEntity.setStatus(File.STATUS_QUEUED);
        fileService.updateById(fileEntity);

        submitForProcessing(fileEntity, description, userId, model);
    }

    private void submitForProcessing(com.flow.model.entity.File fileEntity, String description, String userId,
            String model) {
        // 发送消息到 RabbitMQ
        FileProcessingMessage message = FileProcessingMessage.builder()
                .fileId(fileEntity.getId())
                .userId(userId)
                .model(model)
                .description(description)
                .build();
        rabbitMQProducer.sendFileProcessingMessage(message);
    }

    /**
     * 处理队列中的文件
     */
    @SneakyThrows
    public void processFile(FileProcessingMessage message) {
        Long fileId = message.getFileId();
        com.flow.model.entity.File fileEntity = fileService.getById(fileId);
        if (fileEntity == null) {
            log.error("File not found for id: {}", fileId);
            return;
        }

        String fileName = fileEntity.getName();
        String resourceType;
        String contentType = fileEntity.getType(); // 假设 type 是 content type

        if (contentType == null) {
            // 尝试猜测或失败
            log.warn("Content type is null for file: {}", fileName);
            contentType = "application/octet-stream";
        }

        if (contentType.startsWith("image")) {
            resourceType = "image";
        } else if (contentType.startsWith("video")) {
            resourceType = "video";
        } else if (contentType.startsWith("text")) {
            resourceType = "text";
        } else {
            // 兜底或错误
            resourceType = "unknown";
        }

        // 更新状态为 PROCESSING (处理中)
        fileEntity.setStatus(File.STATUS_PROCESSING);
        fileService.updateById(fileEntity);

        try {
            // 获取外部访问 URL
            String url = ossTemplate.getExternalPresignedUrl(fileName);

            // 2. 生成向量
            List<Double> vector;
            String model = message.getModel();
            if ("image".equals(resourceType)) {
                vector = embeddingService.getImageEmbedding(url, model, 2048);
            } else if ("video".equals(resourceType)) {
                vector = embeddingService.getVideoEmbedding(url, model, 2048);
            } else {
                try (java.io.InputStream is = ossTemplate.getObject(fileEntity.getBucket(), fileEntity.getPath())) {
                    String textContent = new String(is.readAllBytes());
                    if (textContent.length() > 64000) {
                        textContent = textContent.substring(0, 64000);
                    }
                    vector = embeddingService.getTextEmbedding(textContent, model, 2048);
                }
            }

            // 3. 保存到 ES
            MultimodalAsset asset = new MultimodalAsset();
            asset.setId(UUID.randomUUID().toString());
            asset.setUserId(message.getUserId());
            asset.setResourceType(resourceType);
            asset.setUrl(url);
            asset.setFileName(fileName);
            asset.setDescription(message.getDescription());
            asset.setVector(vector);
            asset.setCreateTime(new Date());

            MultimodalAsset savedAsset = multimodalAssetRepository.save(asset);

            // 4. 更新状态为 COMPLETED (完成) 并保存 vectorId
            fileEntity.setVectorId(savedAsset.getId());
            fileEntity.setStatus(File.STATUS_COMPLETED);
            fileService.updateById(fileEntity);

        } catch (Exception e) {
            log.error("Failed to process multimodal asset for file: {}", fileName, e);
            fileEntity.setStatus(File.STATUS_FAILED);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500);
            }
            fileEntity.setErrorMsg(errorMsg);
            fileService.updateById(fileEntity);
            throw e;
        }
    }

    private final ElasticsearchOperations elasticsearchOperations;

    public List<MultimodalAsset> search(String query, String type, String model) {
        List<Double> queryVector;
        if ("image".equalsIgnoreCase(type)) {
            // 如果查询是图片 URL
            queryVector = embeddingService.getImageEmbedding(query, model, 2048);
        } else if ("video".equalsIgnoreCase(type)) {
            // 如果查询是视频 URL
            queryVector = embeddingService.getVideoEmbedding(query, model, 2048);
        } else {
            // 默认为文本
            queryVector = embeddingService.getTextEmbedding(query, model, 2048);
        }

        // 将 List<Double> 转换为 float[] 以供 ES 使用
        float[] vector = new float[queryVector.size()];
        for (int i = 0; i < queryVector.size(); i++) {
            vector[i] = queryVector.get(i).floatValue();
        }

        // 使用 script_score 查询进行余弦相似度计算
        String knnJson = String.format("""
                {
                    "script_score": {
                        "query": { "match_all": {} },
                        "script": {
                            "source": "cosineSimilarity(params.query_vector, 'vector') + 1.0",
                            "params": {
                                "query_vector": %s
                            }
                        }
                    }
                }
                """, new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(vector).toString());

        org.springframework.data.elasticsearch.core.query.StringQuery stringQuery = new org.springframework.data.elasticsearch.core.query.StringQuery(
                knnJson);

        org.springframework.data.elasticsearch.core.SearchHits<MultimodalAsset> searchHits = elasticsearchOperations
                .search(stringQuery, MultimodalAsset.class);

        return searchHits.stream()
                .map(org.springframework.data.elasticsearch.core.SearchHit::getContent)
                .collect(Collectors.toList());
    }
}
