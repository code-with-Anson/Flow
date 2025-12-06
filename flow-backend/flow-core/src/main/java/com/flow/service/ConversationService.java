package com.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.flow.model.entity.AiConversation;
import com.flow.model.entity.AiMessage;

import java.util.List;

/**
 * 对话服务接口
 */
public interface ConversationService extends IService<AiConversation> {

    /**
     * 创建新对话
     */
    AiConversation createConversation(Long userId, String title);

    /**
     * 获取用户所有对话（按更新时间降序）
     */
    List<AiConversation> listUserConversations(Long userId);

    /**
     * 获取对话的所有消息（按创建时间升序）
     */
    List<AiMessage> getConversationMessages(Long conversationId);

    /**
     * 添加消息
     */
    AiMessage addMessage(Long conversationId, String role, String content);

    /**
     * 更新对话标题
     */
    void updateConversationTitle(Long conversationId, String title);

    /**
     * 删除对话
     */
    void deleteConversation(Long conversationId);
}
