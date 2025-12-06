package com.flow.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 对话消息实体
 */
@Data
@TableName("ai_message")
public class AiMessage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long conversationId;

    private String role; // "user" or "ai"

    private String content;

    private Integer tokens;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;
}
