-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY COMMENT 'Primary Key',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Username',
    password VARCHAR(100) NOT NULL COMMENT 'Password',
    nickname VARCHAR(50) COMMENT 'Nickname',
    email VARCHAR(100) COMMENT 'Email',
    avatar VARCHAR(255) COMMENT 'Avatar URL',
    status INT DEFAULT 1 COMMENT 'Status: 1-Enabled, 0-Disabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT COMMENT 'Creator ID',
    update_user BIGINT COMMENT 'Updater ID',
    deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除 0:否 1:是'
);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY COMMENT 'Primary Key',
    name VARCHAR(50) NOT NULL COMMENT 'Role Name',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Role Code',
    description VARCHAR(255) COMMENT 'Description',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT COMMENT 'Creator ID',
    update_user BIGINT COMMENT 'Updater ID'
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 文件表
CREATE TABLE IF NOT EXISTS `sys_file` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `original_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    `size` BIGINT DEFAULT NULL COMMENT '文件大小',
    `type` VARCHAR(100) DEFAULT NULL COMMENT '文件类型',
    `url` VARCHAR(500) DEFAULT NULL COMMENT '文件访问URL',
    `bucket` VARCHAR(100) DEFAULT NULL COMMENT '存储桶',
    `path` VARCHAR(500) DEFAULT NULL COMMENT '存储路径',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除 0:否 1:是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `update_user` BIGINT DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- 初始化角色数据
INSERT IGNORE INTO `sys_role` (`name`, `code`, `description`) VALUES ('管理员', 'ROLE_ADMIN', '系统管理员');
INSERT IGNORE INTO `sys_role` (`name`, `code`, `description`) VALUES ('普通用户', 'ROLE_USER', '普通注册用户');
