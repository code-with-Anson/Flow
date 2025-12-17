package com.flow.service.processor;

import com.flow.ai.service.MultimodalEmbeddingService;
import com.flow.model.dto.FileProcessingMessage;
import com.flow.model.entity.File;
import com.flow.model.es.MultimodalAsset;
import com.flow.oss.OssTemplate;
import com.flow.repository.es.MultimodalAssetRepository;
import com.flow.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class VideoFileProcessor implements FileProcessor {

    private final MultimodalEmbeddingService embeddingService;
    private final MultimodalAssetRepository multimodalAssetRepository;
    private final OssTemplate ossTemplate;
    private final FileService fileService;

    @Override
    public boolean supports(String mimeType) {
        return mimeType != null && mimeType.startsWith("video/");
    }

    @Override
    public void process(File file, FileProcessingMessage message) {
        log.info("Processing video file: {}", file.getName());
        try {
            // 1. 获取公开访问 URL
            String url = ossTemplate.getExternalPresignedUrl(file.getName());

            // 2. 调用 DashScope 获取 Video Embedding (注意：这是整视频理解)
            List<Double> vector = embeddingService.getVideoEmbedding(url, message.getModel(), 2048);

            // 3. 保存到 ES
            MultimodalAsset asset = new MultimodalAsset();
            asset.setId(UUID.randomUUID().toString());
            asset.setUserId(message.getUserId());
            asset.setResourceType("video");
            asset.setUrl(url);
            asset.setFileName(file.getName());
            asset.setDescription(message.getDescription());
            asset.setVector(vector);
            asset.setCreateTime(new Date());

            MultimodalAsset savedAsset = multimodalAssetRepository.save(asset);

            // 4. 更新文件状态
            file.setVectorId(savedAsset.getId());
            file.setStatus(File.STATUS_COMPLETED);
            fileService.updateById(file);
            log.info("Video file processed successfully: {}", file.getName());

        } catch (Exception e) {
            log.error("Failed to process video file: {} - Root cause: {}", file.getName(), e.getMessage(), e);
            file.setStatus(File.STATUS_FAILED);
            String errorMsg = e.getMessage();
            if (e.getCause() != null) {
                errorMsg = e.getCause().getMessage();
            }
            if (errorMsg != null && errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500);
            }
            file.setErrorMsg(errorMsg);
            fileService.updateById(file);
            throw new RuntimeException("Video processing failed: " + errorMsg, e);
        }
    }
}
