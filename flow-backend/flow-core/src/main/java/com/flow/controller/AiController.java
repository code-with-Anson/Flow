package com.flow.controller;

import com.flow.ai.model.dto.ChatSakuraReq;
import com.flow.service.AiService;
import com.flow.common.context.SakuraIdentify;
import com.flow.model.entity.AiConversation;
import com.flow.model.entity.AiMessage;
import com.flow.service.ConversationService;
import com.flow.service.MultimodalSearchService;

import org.springframework.http.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI 模块", description = "AI 对话接口")
@Slf4j
public class AiController {

    @Resource
    private AiService aiService;

    @Resource
    private MultimodalSearchService multimodalSearchService;

    @Resource
    private ConversationService conversationService;

    @Operation(summary = "流式对话")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatSakuraReq chatSakuraReq) {
        log.info("Received stream chat request: {}", chatSakuraReq);
        return aiService.streamChat(chatSakuraReq);
    }

    @Operation(summary = "创建对话")
    @PostMapping("/conversation")
    public AiConversation createConversation(@RequestParam(required = false) String title) {
        Long userId = SakuraIdentify.getCurrentUserId();
        return conversationService.createConversation(userId, title);
    }

    @Operation(summary = "获取对话列表")
    @GetMapping("/conversation")
    public List<AiConversation> listConversations() {
        Long userId = SakuraIdentify.getCurrentUserId();
        return conversationService.listUserConversations(userId);
    }

    @Operation(summary = "获取对话详情")
    @GetMapping("/conversation/{id}")
    public List<AiMessage> getConversationMessages(@PathVariable Long id) {
        return conversationService.getConversationMessages(id);
    }

    @Operation(summary = "更新对话标题")
    @PutMapping("/conversation/{id}/title")
    public void updateConversationTitle(@PathVariable Long id, @RequestParam String title) {
        conversationService.updateConversationTitle(id, title);
    }

    @Operation(summary = "删除对话")
    @DeleteMapping("/conversation/{id}")
    public void deleteConversation(@PathVariable Long id) {
        conversationService.deleteConversation(id);
    }

    @Operation(summary = "摄入知识库文件")
    @PostMapping("/ingest")
    public void ingestFile(@RequestParam Long fileId, @RequestParam(required = false) String description) {
        Long userId = SakuraIdentify.getCurrentUserId();
        // Use default model or specific one if needed
        multimodalSearchService.processUploadedFile(fileId, description, String.valueOf(userId), null);
    }
}
