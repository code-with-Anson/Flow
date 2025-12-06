package com.flow.ai.strategy;

import com.flow.ai.model.dto.ChatSakuraReq;
import com.flow.ai.model.enums.AiProvider;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiStrategy {

    Flux<String> streamChat(ChatSakuraReq request, List<Message> history);

    boolean supports(AiProvider provider);
}
