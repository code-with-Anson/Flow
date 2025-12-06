package com.flow.service.impl;

import com.flow.ai.factory.AiStrategyFactory;
import com.flow.ai.model.dto.ChatSakuraReq;
import com.flow.service.AiService;
import com.flow.model.entity.AiMessage;
import com.flow.service.ConversationService;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiServiceImpl implements AiService {

    private final AiStrategyFactory aiStrategyFactory;
    private final ConversationService conversationService;

    public AiServiceImpl(AiStrategyFactory aiStrategyFactory, ConversationService conversationService) {
        this.aiStrategyFactory = aiStrategyFactory;
        this.conversationService = conversationService;
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
                    return historyMessages;
                }).subscribeOn(Schedulers.boundedElastic()))
                .defaultIfEmpty(Collections.emptyList())
                .flatMapMany(history -> {
                    StringBuilder aiResponseBuilder = new StringBuilder();
                    // 3. 调用 AI - 这里 history 是 List<Message>
                    return aiStrategyFactory.getStrategy(request.getProvider())
                            .streamChat(request, history)
                            .doOnNext(aiResponseBuilder::append)
                            .doOnComplete(() -> {
                                // 4. 保存 AI 回复 (阻塞)
                                if (conversationId != null && aiResponseBuilder.length() > 0) {
                                    Mono.fromRunnable(() -> conversationService.addMessage(conversationId, "ai",
                                            aiResponseBuilder.toString())).subscribeOn(Schedulers.boundedElastic())
                                            .subscribe();
                                }
                            });
                })
                .switchIfEmpty(
                        aiStrategyFactory.getStrategy(request.getProvider()).streamChat(request,
                                Collections.emptyList()));
    }
}
