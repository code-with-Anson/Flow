package com.flow.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.ai.config.DashScopeConfig;
import com.flow.ai.service.MultimodalEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashScopeEmbeddingServiceImpl implements MultimodalEmbeddingService {

    private final DashScopeConfig dashScopeConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/multimodal-embedding/multimodal-embedding";
    // private static final String DEFAULT_MODEL = "qwen2.5-vl-embedding"; //
    // 移除硬编码常量
    // private static final int DEFAULT_DIMENSION = 2048; // 移除硬编码常量

    @Override
    public List<Double> getTextEmbedding(String text, String model, Integer dimension) {
        Map<String, Object> content = new HashMap<>();
        content.put("text", text);
        return callApi(content, model, dimension);
    }

    @Override
    public List<Double> getImageEmbedding(String imageUrl, String model, Integer dimension) {
        Map<String, Object> content = new HashMap<>();
        content.put("image", imageUrl);
        return callApi(content, model, dimension);
    }

    @Override
    public List<Double> getVideoEmbedding(String videoUrl, String model, Integer dimension) {
        Map<String, Object> content = new HashMap<>();
        content.put("video", videoUrl);
        return callApi(content, model, dimension);
    }

    private List<Double> callApi(Map<String, Object> contentItem, String model, Integer dimension) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + dashScopeConfig.getApiKey());

            Map<String, Object> input = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            contents.add(contentItem);
            input.put("contents", contents);

            Map<String, Object> requestBody = new HashMap<>();
            String modelName = model != null ? model : dashScopeConfig.getModel();
            requestBody.put("model", modelName);
            requestBody.put("input", input);

            // 可选参数
            Map<String, Object> parameters = new HashMap<>();
            int dim = dimension != null ? dimension : dashScopeConfig.getDimension();
            parameters.put("dimension", dim);
            requestBody.put("parameters", parameters);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Calling DashScope API with model: {}", modelName);
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.has("output") && root.get("output").has("embeddings")) {
                    JsonNode embeddings = root.get("output").get("embeddings");
                    if (embeddings.isArray() && embeddings.size() > 0) {
                        JsonNode embeddingNode = embeddings.get(0).get("embedding");
                        List<Double> vector = new ArrayList<>();
                        if (embeddingNode.isArray()) {
                            for (JsonNode val : embeddingNode) {
                                vector.add(val.asDouble());
                            }
                        }
                        return vector;
                    }
                }
                // 即使状态码是 200，也要检查响应体中的错误（某些 API 会这样做）
                if (root.has("code") && root.has("message")) {
                    throw new RuntimeException(
                            "DashScope API Error: " + root.get("code").asText() + " - " + root.get("message").asText());
                }
            }

            throw new RuntimeException("Failed to get embedding from DashScope. Status: " + response.getStatusCode());

        } catch (Exception e) {
            log.error("Error calling DashScope API", e);
            throw new RuntimeException("Error generating embedding", e);
        }
    }
}
