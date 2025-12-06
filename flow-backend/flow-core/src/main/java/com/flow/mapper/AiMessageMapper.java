package com.flow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flow.model.entity.AiMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 对话消息 Mapper
 */
@Mapper
public interface AiMessageMapper extends BaseMapper<AiMessage> {
}
