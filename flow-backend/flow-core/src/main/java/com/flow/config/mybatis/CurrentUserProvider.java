package com.flow.config.mybatis;

/**
 * 提供当前用户 ID 的接口
 * 用于 MyBatis-Plus 自动填充审计字段
 */
public interface CurrentUserProvider {
    Long getCurrentUserId();
}
