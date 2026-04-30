package com.jingwei.common.domain.model;

/**
 * 用户上下文工具类
 * <p>
 * 基于 ThreadLocal 存储当前登录用户ID，供审计字段自动填充使用。
 * 在 JWT Filter 中设置，在请求结束后清理。
 * </p>
 *
 * @author JingWei
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取当前用户ID
     *
     * @return 当前用户ID，未登录时返回 null
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 清除当前用户ID
     * <p>
     * 必须在请求结束时调用，防止内存泄漏。
     * </p>
     */
    public static void clear() {
        USER_ID.remove();
    }
}
