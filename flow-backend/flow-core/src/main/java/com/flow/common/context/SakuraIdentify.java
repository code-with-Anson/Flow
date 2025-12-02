package com.flow.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 樱花身份信息 - 存储当前登录用户的信息
 * 基于 ThreadLocal 实现，确保线程安全
 *
 * @author Anson
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SakuraIdentify {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * ThreadLocal 存储当前用户信息
     */
    private static final ThreadLocal<SakuraIdentify> CONTEXT = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     *
     * @param identify 用户身份信息
     */
    public static void set(SakuraIdentify identify) {
        CONTEXT.set(identify);
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户身份信息
     */
    public static SakuraIdentify get() {
        return CONTEXT.get();
    }

    /**
     * 获取当前用户ID
     *
     * @return 当前用户ID
     */
    public static Long getCurrentUserId() {
        SakuraIdentify identify = get();
        return identify != null ? identify.getUserId() : null;
    }

    /**
     * 获取当前用户名
     *
     * @return 当前用户名
     */
    public static String getCurrentUsername() {
        SakuraIdentify identify = get();
        return identify != null ? identify.getUsername() : null;
    }

    /**
     * 清除当前用户信息
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
