package com.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flow.mapper.AiConversationMapper;
import com.flow.mapper.AiMessageMapper;
import com.flow.model.entity.AiConversation;
import com.flow.model.entity.AiMessage;
import com.flow.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 对话服务实现
 */
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<AiConversationMapper, AiConversation>
        implements ConversationService {

    private final AiMessageMapper aiMessageMapper;

    @Override
    @Transactional
    public AiConversation createConversation(Long userId, String title) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle(title != null ? title : "新对话");
        conversation.setModel("gemini-2.5-flash");
        this.save(conversation);
        return conversation;
    }

    @Override
    public List<AiConversation> listUserConversations(Long userId) {
        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConversation::getUserId, userId)
                .orderByDesc(AiConversation::getUpdateTime);
        return this.list(wrapper);
    }

    @Override
    public List<AiMessage> getConversationMessages(Long conversationId) {
        LambdaQueryWrapper<AiMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMessage::getConversationId, conversationId)
                .orderByAsc(AiMessage::getCreateTime);
        return aiMessageMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public AiMessage addMessage(Long conversationId, String role, String content) {
        AiMessage message = new AiMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setTokens(0); // TODO: Calculate tokens
        aiMessageMapper.insert(message);

        // 更新对话的 update_time
        AiConversation conversation = this.getById(conversationId);
        if (conversation != null) {
            this.updateById(conversation);
        }

        return message;
    }

    @Override
    @Transactional
    public void updateConversationTitle(Long conversationId, String title) {
        AiConversation conversation = this.getById(conversationId);
        if (conversation != null) {
            conversation.setTitle(title);
            this.updateById(conversation);
        }
    }

    @Override
    @Transactional
    public void deleteConversation(Long conversationId) {
        this.removeById(conversationId);
    }
}
