-- AI 对话会话表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
    `id` BIGINT NOT NULL COMMENT '主键ID（雪花ID）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(255) DEFAULT '新对话' COMMENT '对话标题',
    `model` VARCHAR(50) DEFAULT 'gemini-2.5-flash' COMMENT '使用的AI模型',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `create_user` BIGINT COMMENT '创建人',
    `update_user` BIGINT COMMENT '更新人',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话表';

-- AI 对话消息表
CREATE TABLE IF NOT EXISTS `ai_message` (
    `id` BIGINT NOT NULL COMMENT '主键ID（雪花ID）',
    `conversation_id` BIGINT NOT NULL COMMENT '所属对话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色: user/ai',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `tokens` INT DEFAULT 0 COMMENT 'Token数量',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_conversation_id` (`conversation_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';
