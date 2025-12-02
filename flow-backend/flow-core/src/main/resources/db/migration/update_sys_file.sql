-- 1. 添加文件哈希，用于秒传和去重
ALTER TABLE `sys_file` ADD COLUMN `file_hash` VARCHAR(64) DEFAULT NULL COMMENT '文件 SHA256/MD5 哈希' AFTER `original_name`;

-- 2. 添加状态机字段，控制向量化流程
-- 0: UPLOADING (上传中/待处理)
-- 1: PROCESSING (AI 处理中/向量化中)
-- 2: COMPLETED (处理完成，可检索)
-- -1: FAILED (处理失败)
ALTER TABLE `sys_file` ADD COLUMN `status` TINYINT DEFAULT 0 COMMENT '处理状态' AFTER `type`;

-- 3. 添加向量库关联 ID (方便后续删除文件时同步删除向量)
ALTER TABLE `sys_file` ADD COLUMN `vector_id` VARCHAR(64) DEFAULT NULL COMMENT '向量库 ID (Qdrant/ES)' AFTER `status`;

-- 4. 添加错误信息记录
ALTER TABLE `sys_file` ADD COLUMN `error_msg` TEXT DEFAULT NULL COMMENT '处理失败原因' AFTER `vector_id`;

-- 5. 为查询性能添加索引
CREATE INDEX `idx_file_hash` ON `sys_file` (`file_hash`);
-- 6. 删除冗余的 URL 列 (动态生成，不存储)
ALTER TABLE `sys_file` DROP COLUMN `url`;
