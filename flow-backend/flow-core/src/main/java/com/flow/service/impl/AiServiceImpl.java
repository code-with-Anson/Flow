package com.flow.service.impl;

import com.flow.ai.model.dto.ChatSakuraReq;
import com.flow.service.AiService;
import com.flow.service.MultimodalSearchService;
import com.flow.model.es.MultimodalAsset;
import com.flow.model.entity.AiMessage;
import com.flow.service.ConversationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiServiceImpl implements AiService {

    private final ConversationService conversationService;
    private final MultimodalSearchService multimodalSearchService;
    private final com.flow.factory.DynamicAiFactory dynamicAiFactory;

    @Value("${ai.rag.context-limit:6000}")
    private int ragContextLimit;

    public AiServiceImpl(ConversationService conversationService,
            MultimodalSearchService multimodalSearchService, com.flow.factory.DynamicAiFactory dynamicAiFactory) {
        this.conversationService = conversationService;
        this.multimodalSearchService = multimodalSearchService;
        this.dynamicAiFactory = dynamicAiFactory;
    }

    @Override
    public Flux<String> streamChat(ChatSakuraReq request) {
        Long conversationId = request.getConversationId();

        return Mono.justOrEmpty(conversationId)
                .flatMap(id -> Mono.fromRunnable(() -> {
                    // 2.1 保存用户消息 (阻塞)
                    conversationService.addMessage(id, "user", request.getMessage());
                }).subscribeOn(Schedulers.boundedElastic()).thenReturn(id))
                .flatMap(id -> Mono.fromCallable(() -> {
                    // 2.2 加载历史消息 (阻塞) -> 取最近 10 条
                    List<AiMessage> allMessages = conversationService.getConversationMessages(id);

                    int limit = 10;
                    int size = allMessages.size();
                    // 排除刚发的这条
                    if (size > 0 && allMessages.get(size - 1).getRole().equals("user")
                            && allMessages.get(size - 1).getContent().equals(request.getMessage())) {
                        allMessages.remove(size - 1);
                    }

                    List<AiMessage> historyEntities = allMessages.stream()
                            .skip(Math.max(0, allMessages.size() - limit))
                            .collect(Collectors.toList());

                    // Convert to Spring AI Messages
                    List<Message> historyMessages = new ArrayList<>();
                    for (AiMessage msg : historyEntities) {
                        if ("user".equalsIgnoreCase(msg.getRole())) {
                            historyMessages.add(new UserMessage(msg.getContent()));
                        } else if ("ai".equalsIgnoreCase(msg.getRole())) {
                            historyMessages.add(new AssistantMessage(msg.getContent()));
                        }
                    }

                    // RAG Logic
                    if (Boolean.TRUE.equals(request.getUseKnowledgeBase())) {
                        try {
                            // Search for relevant documents
                            List<MultimodalAsset> assets = multimodalSearchService.search(request.getMessage(), "text",
                                    null);

                            if (assets != null && !assets.isEmpty()) {
                                StringBuilder ragContext = new StringBuilder();
                                ragContext.append("基于以下知识库内容回答用户问题：\n");
                                for (int i = 0; i < assets.size(); i++) {
                                    MultimodalAsset asset = assets.get(i);
                                    ragContext.append("---\n");
                                    ragContext.append("[").append(i + 1).append("] 类型: ")
                                            .append(asset.getResourceType()).append(", 文件名: ")
                                            .append(asset.getFileName()).append("\n");

                                    // 对于 Image/Video，提供 URL
                                    if ("image".equals(asset.getResourceType())
                                            || "video".equals(asset.getResourceType())) {
                                        ragContext.append("链接: ").append(asset.getUrl()).append("\n");
                                    }

                                    // Use Description and truncate based on config
                                    String content = asset.getDescription();
                                    if (content != null && !content.isEmpty()) {
                                        if (content.length() > ragContextLimit) {
                                            content = content.substring(0, ragContextLimit) + "...";
                                        }
                                        ragContext.append("描述/内容: ").append(content).append("\n");
                                    }
                                }
                                ragContext.append("---\n");
                                ragContext.append("用户问题: ").append(request.getMessage()).append("\n");
                                ragContext.append(
                                        "(说明) 如果参考资料中包含图片或视频链接 (URL)，请务必在回答中通过 Markdown 格式 (如 ![title](url)) 将其展示出来，并结合描述进行回答。");

                                // Insert as System Message at the start of history
                                historyMessages.add(0, new SystemMessage(ragContext.toString()));
                            }
                        } catch (Exception e) {
                            System.err.println("RAG Search failed: " + e.getMessage());
                        }
                    }

                    return historyMessages;
                }).subscribeOn(Schedulers.boundedElastic()))
                .defaultIfEmpty(Collections.emptyList())
                .flatMapMany(history -> {
                    StringBuilder aiResponseBuilder = new StringBuilder();
                    Flux<String> responseFlux;

                    if (request.getProviderId() != null) {
                        // Dynamic Path via DynamicAiFactory
                        ChatClient client = dynamicAiFactory.getChatClient(request.getProviderId());

                        List<Message> messages = new ArrayList<>();
                        messages.add(new SystemMessage("你是一位友善的 AI 助手，能够帮助用户回答问题、提供信息。"));
                        if (history != null)
                            messages.addAll(history);
                        messages.add(new UserMessage(request.getMessage()));

                        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder();
                        if (StringUtils.hasText(request.getModel())) {
                            optionsBuilder.withModel(request.getModel());
                        }

                        // Fix for temperature lint if needed, but here we just use what we have or
                        // defaults
                        // Note: DynamicAiFactory sets defaults, Prompt options override them.

                        Prompt prompt = new Prompt(messages, optionsBuilder.build());

                        responseFlux = client.prompt(prompt)
                                .stream()
                                .content()
                                .onErrorResume(e -> Flux.just("Dynamic AI Error: " + e.getMessage()));
                    } else {
                        // Strict Mode: Legacy usage is forbidden
                        throw new IllegalArgumentException(
                                "Strict Mode Enabled: Provider ID is required. Please check your settings.");
                    }

                    // 3. 调用 AI & 4. 保存回复
                    return responseFlux
                            .doOnNext(aiResponseBuilder::append)
                            .doOnComplete(() -> {
                                if (conversationId != null && aiResponseBuilder.length() > 0) {
                                    Mono.fromRunnable(() -> conversationService.addMessage(conversationId, "ai",
                                            aiResponseBuilder.toString())).subscribeOn(Schedulers.boundedElastic())
                                            .subscribe();
                                }
                            });
                })
                .switchIfEmpty(
                        // Fallback handling if history loading failed somehow, but usually flatMapMany
                        // handles empty Flux gracefully by not emitting.
                        // However, defaultIfEmpty above ensures history is at least empty list.
                        // We still need to run the AI logic if history was empty.
                        // The above flatMapMany logic covers both cases (history list is just empty).
                        // So we might not need switchIfEmpty logic duplication if we structured it
                        // right.
                        // But to strictly follow the previous pattern where switchIfEmpty was used for
                        // cached empty list:
                        Flux.defer(() -> {
                            // This block is only hit if the MONO is empty, but we used
                            // defaultIfEmpty(Collections.emptyList())
                            // So the Mono<List> is never empty.
                            // Thus flatMapMany is always executed.
                            // So the original switchIfEmpty usage was likely redundant or I misunderstood.
                            // Wait, existing code:
                            // .defaultIfEmpty(Collections.emptyList()).flatMapMany(...) .switchIfEmpty(...)
                            // If defaultIfEmpty is there, switchIfEmpty on the Mono chain is reachable?
                            // No, flatMapMany returns a Flux. code was:
                            // .defaultIfEmpty(Collections.emptyList()).flatMapMany(...).switchIfEmpty(...)
                            // The switchIfEmpty is on the FLUX returned by flatMapMany?
                            // If flatMapMany returns empty Flux?
                            // No, flatMapMany returns the result of the lambda.
                            // Our lambda always returns a Flux (from Strategy).
                            // So we don't need switchIfEmpty unless the Strategy returns empty flux.
                            return Flux.empty();
                        }));
    }
}
