package com.flow.ai.model.dto;

import com.flow.ai.model.enums.AiProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChatSakuraReq {

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
}
