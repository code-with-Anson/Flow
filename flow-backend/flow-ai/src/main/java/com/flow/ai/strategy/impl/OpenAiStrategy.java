package com.flow.ai.strategy.impl;

import com.flow.ai.model.dto.ChatSakuraReq;
import com.flow.ai.model.enums.AiProvider;
import com.flow.ai.strategy.AiStrategy;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Component
public class OpenAiStrategy implements AiStrategy {

    private final OpenAiChatModel openAiChatModel;

    public OpenAiStrategy(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    @Override
    public Flux<String> streamChat(ChatSakuraReq request, List<Message> history) {
        List<Message> messages = new ArrayList<>();

        // 1. System Prompt
        messages.add(new SystemMessage("你是一位友善的 AI 助手，能够帮助用户回答问题、提供信息。"));

        // 2. Load History
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }

        // 3. Current User Message
        messages.add(new UserMessage(request.getMessage()));

        // 4. Create Prompt and Stream
        return getChatClient(request).prompt(new Prompt(messages))
                .stream()
                .content()
                .onErrorResume(e -> {
                    return Flux.just("项目有些小问题... (Error: " + e.getMessage() + ")");
                });
    }

    @Override
    public boolean supports(AiProvider provider) {
        return provider == AiProvider.OPENAI;
    }

    private ChatClient getChatClient(ChatSakuraReq request) {
        // 1. 如果前端传递了自定义配置 (BaseURL 或 API Key)，则动态创建 Client
        if (StringUtils.hasText(request.getApiKey()) || StringUtils.hasText(request.getBaseUrl())) {
            String baseUrl = StringUtils.hasText(request.getBaseUrl())
                    ? request.getBaseUrl()
                    : "https://api.openai.com";

            String apiKey = StringUtils.hasText(request.getApiKey())
                    ? request.getApiKey()
                    : "dummy-key";

            OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .withModel(StringUtils.hasText(request.getModel()) ? request.getModel() : "gpt-3.5-turbo")
                    .build();
            OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi, options);
            return ChatClient.create(chatModel);
        }

        // 2. 否则使用预配置的 Bean
        return ChatClient.create(openAiChatModel);
    }
}
