package com.flow.ai.model.dto;

import com.flow.ai.model.enums.AiProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

public class ChatSakuraReq {

    @Schema(description = "对话ID（可选，新对话不传）")
    private Long conversationId;

    @Schema(description = "用户消息", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "模型名称 (可选)")
    private String model;

    @Schema(description = "API Base URL (可选)")
    private String baseUrl;

    @Schema(description = "API Key (可选)")
    private String apiKey;

    @Schema(description = "AI 提供商", defaultValue = "OPENAI")
    private AiProvider provider = AiProvider.OPENAI;

    @Schema(description = "是否启用知识库")
    private Boolean useKnowledgeBase;

    public Boolean getUseKnowledgeBase() {
        return useKnowledgeBase;
    }

    public void setUseKnowledgeBase(Boolean useKnowledgeBase) {
        this.useKnowledgeBase = useKnowledgeBase;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public AiProvider getProvider() {
        return provider;
    }

    public void setProvider(AiProvider provider) {
        this.provider = provider;
    }
}
