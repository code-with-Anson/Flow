package com.flow.factory;

import com.flow.ai.model.enums.AiProvider;
import com.flow.common.util.AESUtils;
import com.flow.model.entity.AiProviderConfig;
import com.flow.service.AiProviderService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DynamicAiFactory {

    private final AiProviderService aiProviderService;
    private final AESUtils aesUtils;

    /**
     * LRU 缓存：最多 100 个 ChatClient，30分钟不访问自动过期
     */
    private final Cache<Long, ChatClient> clientCache;

    public DynamicAiFactory(AiProviderService aiProviderService, AESUtils aesUtils) {
        this.aiProviderService = aiProviderService;
        this.aesUtils = aesUtils;
        this.clientCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .evictionListener(
                        (key, value, cause) -> log.info("ChatClient evicted: providerId={}, cause={}", key, cause))
                .build();
    }

    /**
     * 根据 Provider ID 获取或创建 ChatClient
     */
    public ChatClient getChatClient(Long providerId) {
        if (providerId == null) {
            throw new IllegalArgumentException("Provider ID cannot be null");
        }

        return clientCache.get(providerId, this::createClient);
    }

    /**
     * 清除指定 Provider 的缓存 (当配置更新时调用)
     */
    public void invalidate(Long providerId) {
        clientCache.invalidate(providerId);
        log.info("Invalidated ChatClient cache for providerId: {}", providerId);
    }

    /**
     * 清除全部缓存 (当需要重置所有连接时调用)
     */
    public void clearAll() {
        long size = clientCache.estimatedSize();
        clientCache.invalidateAll();
        log.info("Cleared all ChatClient cache, total: {} entries", size);
    }

    /**
     * 获取当前缓存大小
     */
    public long getCacheSize() {
        return clientCache.estimatedSize();
    }

    private ChatClient createClient(Long providerId) {
        log.info("Creating new ChatClient for providerId: {}", providerId);
        AiProviderConfig config = aiProviderService.getById(providerId);
        if (config == null) {
            throw new IllegalArgumentException("AI Provider config not found for id: " + providerId);
        }

        AiProvider providerType = config.getProvider();

        // 根据 Provider 类型创建不同的 Client
        return switch (providerType) {
            case OPENAI, OLLAMA -> createOpenAiCompatibleClient(config);
            // 预留其他类型扩展
            // case ANTHROPIC -> createAnthropicClient(config);
            // case GEMINI -> createGeminiClient(config);
            default -> {
                log.warn("Unknown provider type: {}, falling back to OpenAI compatible", providerType);
                yield createOpenAiCompatibleClient(config);
            }
        };
    }

    /**
     * 创建 OpenAI 兼容的 ChatClient (支持 OpenAI, DeepSeek, OpenRouter, Ollama 等)
     */
    private ChatClient createOpenAiCompatibleClient(AiProviderConfig config) {
        String baseUrl = config.getBaseUrl();
        String apiKey = aesUtils.decrypt(config.getApiKey());

        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "https://api.openai.com";
        }
        if (!StringUtils.hasText(apiKey)) {
            apiKey = "dummy";
        }

        log.debug("Creating OpenAI compatible client with baseUrl: {}", baseUrl);
        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);

        // 默认 Options，实际请求时可以覆盖 Model
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel("gpt-3.5-turbo") // Default fallback
                .withTemperature(0.7f)
                .build();

        OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi, options);
        return ChatClient.create(chatModel);
    }
}
