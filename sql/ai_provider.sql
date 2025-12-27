-- AI Provider Configuration Table
CREATE TABLE IF NOT EXISTS `ai_provider_config` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(64) NOT NULL COMMENT '配置名称',
  `provider` varchar(32) NOT NULL COMMENT '供应商类型 (OPENAI, OLLAMA, OTHER)',
  `base_url` varchar(255) DEFAULT NULL COMMENT 'API Base URL',
  `api_key` varchar(512) DEFAULT NULL COMMENT 'API Key (Encrypted)',
  `models` text DEFAULT NULL COMMENT '可用模型列表 (JSON)',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `priority` int(11) DEFAULT 0 COMMENT '排序优先级',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user` bigint(20) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 供应商配置';
