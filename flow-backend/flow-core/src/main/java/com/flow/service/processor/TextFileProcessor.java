package com.flow.service.processor;

import com.flow.ai.service.MultimodalEmbeddingService;
import com.flow.model.dto.FileProcessingMessage;
import com.flow.model.entity.File;
import com.flow.model.es.MultimodalAsset;
import com.flow.config.oss.OssTemplate;
import com.flow.repository.es.MultimodalAssetRepository;
import com.flow.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class TextFileProcessor implements FileProcessor {

    private final MultimodalEmbeddingService embeddingService;
    private final MultimodalAssetRepository multimodalAssetRepository;
    private final OssTemplate ossTemplate;
    private final FileService fileService;

    private static final int CHUNK_SIZE = 500; // 简单的字符长度切分，实际可用 Tokenizer

    @Override
    public boolean supports(String mimeType) {
        // 支持文本、PDF、Word等 (这里简化为 generic text handling logic)
        // 实际上需要 Tika 来解析 PDF/Word，这里假设 ParseService 已经集成或者先处理简单文本
        return mimeType != null && (mimeType.startsWith("text/") ||
                mimeType.equals("application/pdf") ||
                mimeType.contains("word"));
    }

    @Override
    public void process(File file, FileProcessingMessage message) {
        log.info("Processing text file: {}", file.getName());
        try {
            // 1. 读取文件内容 (这里简化为直接读取文本，实际应调用 ParseService/Tika)
            String content;
            try (InputStream is = ossTemplate.getObject(file.getBucket(), file.getPath())) {
                // TODO: 集成 Tika 解析 PDF/Word，暂时只处理纯文本读取
                content = new String(is.readAllBytes());
            }

            if (content.isEmpty()) {
                log.warn("Empty content for file: {}", file.getName());
                return;
            }

            // 2. 切分 (Chunking)
            int totalLength = content.length();
            int chunks = (int) Math.ceil((double) totalLength / CHUNK_SIZE);
            String url = ossTemplate.getExternalPresignedUrl(file.getName());

            for (int i = 0; i < chunks; i++) {
                int start = i * CHUNK_SIZE;
                int end = Math.min(start + CHUNK_SIZE, totalLength);
                String chunkText = content.substring(start, end);

                if (chunkText.trim().isEmpty())
                    continue;

                // 3. 向量化
                List<Double> vector = embeddingService.getTextEmbedding(chunkText, message.getModel(), 2048);

                // 4. 保存 Chunk 到 ES
                MultimodalAsset asset = new MultimodalAsset();
                asset.setId(UUID.randomUUID().toString());
                asset.setUserId(message.getUserId());
                asset.setResourceType("text");
                asset.setUrl(url); // 指向源文件
                asset.setFileName(file.getName());
                // 对于文本块，description 存储块内容
                asset.setDescription(chunkText);
                asset.setVector(vector);
                asset.setCreateTime(new Date());

                multimodalAssetRepository.save(asset);
            }

            // 5. 更新文件状态
            file.setStatus(File.STATUS_COMPLETED);
            // 文本文件会产生多个 vector，这里只记录最后状态，或者不记录单一 vectorId
            fileService.updateById(file);
            log.info("Text file processed successfully: {}, chunks: {}", file.getName(), chunks);

        } catch (Exception e) {
            log.error("Failed to process text file: {} - Root cause: {}", file.getName(), e.getMessage(), e);
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
            throw new RuntimeException("Text processing failed: " + errorMsg, e);
        }
    }
}
