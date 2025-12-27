package com.flow.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.flow.ai.model.enums.AiProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 供应商配置实体
 * 存储用户自定义的 AI 渠道信息 (如 DeepSeek, OpenRouter, Local Ollama)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_provider_config")
public class AiProviderConfig extends AkatsukiOperator {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 配置名称 (e.g. "My DeepSeek")
     */
    private String name;

    /**
     * 供应商类型 (OPENAI, OLLAMA...)
     */
    private AiProvider provider;

    /**
     * Base URL
     */
    private String baseUrl;

    /**
     * API Key (应加密存储，此处简化)
     */
    private String apiKey;

    /**
     * 可用模型列表 (JSON String or comma separated)
     */
    private String models;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 排序优先级
     */
    private Integer priority;

    @TableLogic
    private Integer deleted;
}
