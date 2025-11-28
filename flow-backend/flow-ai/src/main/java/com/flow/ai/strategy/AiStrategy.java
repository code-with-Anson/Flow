package com.flow.ai.strategy;

import com.flow.ai.model.dto.ChatSakuraReq;
import com.flow.ai.model.enums.AiProvider;
import reactor.core.publisher.Flux;

public interface AiStrategy {

    Flux<String> streamChat(ChatSakuraReq request);

    boolean supports(AiProvider provider);
}
