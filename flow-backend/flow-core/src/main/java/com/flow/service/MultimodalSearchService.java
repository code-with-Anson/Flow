package com.flow.service;

import com.flow.ai.service.MultimodalEmbeddingService;
import com.flow.service.processor.FileProcessor;
import com.flow.model.entity.File;
import com.flow.model.es.MultimodalAsset;
import com.flow.oss.OssTemplate;
import com.flow.repository.es.MultimodalAssetRepository;
import com.flow.model.dto.FileProcessingMessage;
import com.flow.mq.RabbitMQProducer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import java.util.stream.Collectors;

import com.flow.common.enums.ErrorCode;
import com.flow.common.exception.BusinessException;

@Service
@Slf4j
@RequiredArgsConstructor
public class MultimodalSearchService {

    private final MultimodalEmbeddingService embeddingService; // used by search
    private final FileService fileService;
    private final RabbitMQProducer rabbitMQProducer;
    private final List<FileProcessor> fileProcessors;

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

        // 更新状态为 PROCESSING
        fileEntity.setStatus(File.STATUS_PROCESSING);
        fileService.updateById(fileEntity);

        String mimeType = fileEntity.getType();
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        try {
            // 策略分发
            boolean processed = false;
            for (FileProcessor processor : fileProcessors) {
                if (processor.supports(mimeType)) {
                    processor.process(fileEntity, message);
                    processed = true;
                    break;
                }
            }

            if (!processed) {
                log.warn("No processor found for mime type: {}", mimeType);
                // 可以选择抛错或者标记为 UNSUPPORTED
                fileEntity.setStatus(File.STATUS_FAILED);
                fileEntity.setErrorMsg("Unsupported file type: " + mimeType);
                fileService.updateById(fileEntity);
            }
        } catch (Exception e) {
            log.error("Failed to process file: {}", fileEntity.getName(), e);
            fileEntity.setStatus(File.STATUS_FAILED);
            fileEntity.setErrorMsg(e.getMessage());
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

        StringQuery stringQuery = new StringQuery(knnJson);

        SearchHits<MultimodalAsset> searchHits = elasticsearchOperations
                .search(stringQuery, MultimodalAsset.class);

        return searchHits.stream()
                .map(org.springframework.data.elasticsearch.core.SearchHit::getContent)
                .collect(Collectors.toList());
    }
}
