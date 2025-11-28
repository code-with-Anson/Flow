package com.flow.ai.controller;

import com.flow.ai.model.dto.ChatSakuraReq;
import com.flow.ai.service.AiService;
import org.springframework.http.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI 模块", description = "AI 对话接口")
@Slf4j
public class AiController {

    @Resource
    private AiService aiService;

    @Operation(summary = "流式对话")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatSakuraReq chatSakuraReq) {
        log.info("Received stream chat request: {}", chatSakuraReq);
        return aiService.streamChat(chatSakuraReq);
    }
}
