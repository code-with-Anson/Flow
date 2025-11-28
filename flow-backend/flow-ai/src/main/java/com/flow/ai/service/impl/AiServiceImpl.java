package com.flow.ai.service.impl;

import com.flow.ai.factory.AiStrategyFactory;
import com.flow.ai.model.dto.ChatSakuraReq;
import com.flow.ai.service.AiService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiServiceImpl implements AiService {

    private final AiStrategyFactory aiStrategyFactory;

    public AiServiceImpl(AiStrategyFactory aiStrategyFactory) {
        this.aiStrategyFactory = aiStrategyFactory;
    }

    @Override
    public Flux<String> streamChat(ChatSakuraReq request) {
        return aiStrategyFactory.getStrategy(request.getProvider()).streamChat(request);
    }
}
