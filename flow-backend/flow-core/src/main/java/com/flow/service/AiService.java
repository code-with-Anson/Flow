package com.flow.service;

import com.flow.ai.model.dto.ChatSakuraReq;
import reactor.core.publisher.Flux;

public interface AiService {

    Flux<String> streamChat(ChatSakuraReq request);
}
