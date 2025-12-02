package com.flow.service;

import com.flow.ai.service.MultimodalEmbeddingService;
import com.flow.model.es.MultimodalAsset;
import com.flow.oss.OssTemplate;
import com.flow.repository.es.MultimodalAssetRepository;
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

    @SneakyThrows
    public MultimodalAsset uploadAndIndex(MultipartFile file, String description, String userId, String model) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("Content type is null");
        }

        String resourceType;
        // 0. 校验文件大小和类型
        if (contentType.startsWith("image")) {
            resourceType = "image";
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT, "图片大小不能超过5MB");
            }
        } else if (contentType.startsWith("video")) {
            resourceType = "video";
            // DashScope 限制视频大小为 50MB (50700KB)
            if (file.getSize() > 50 * 1024 * 1024) {
                throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDS_LIMIT, "视频大小不能超过50MB");
            }
        } else if (contentType.startsWith("text")) {
            resourceType = "text";
        } else {
            throw new IllegalArgumentException("Unsupported content type: " + contentType);
        }

        // 1. 上传到 MinIO
        String fileName = ossTemplate.uploadFile(file);
        // 获取外部访问 URL (如果有配置 external-endpoint，则使用外部 MinIO 客户端生成带正确签名的 URL)
        String url = ossTemplate.getExternalPresignedUrl(fileName);

        // 2. 生成向量
        List<Double> vector;
        if ("image".equals(resourceType)) {
            vector = embeddingService.getImageEmbedding(url, model, 2048);
        } else if ("video".equals(resourceType)) {
            vector = embeddingService.getVideoEmbedding(url, model, 2048);
        } else {
            // text
            String textContent = new String(file.getBytes());
            if (textContent.length() > 64000) {
                textContent = textContent.substring(0, 64000);
            }
            vector = embeddingService.getTextEmbedding(textContent, model, 2048);
        }

        // 3. 保存到 ES
        MultimodalAsset asset = new MultimodalAsset();
        asset.setId(UUID.randomUUID().toString());
        asset.setUserId(userId);
        asset.setResourceType(resourceType);
        asset.setUrl(url);
        asset.setFileName(fileName);
        asset.setDescription(description);
        asset.setVector(vector);
        asset.setCreateTime(new Date());

        return multimodalAssetRepository.save(asset);
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
