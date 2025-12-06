package com.flow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flow.model.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 对话会话 Mapper
 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {
}
