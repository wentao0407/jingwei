package com.jingwei.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.jingwei.common.domain.model.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 审计字段自动填充处理器
 * <p>
 * 插入时自动填充：createdBy, createdAt, updatedBy, updatedAt, deleted
 * 更新时自动填充：updatedBy, updatedAt
 * <p>
 * 当前用户ID从 {@link UserContext} 获取，由 JWT Filter 设置。
 * 未登录场景下 userId 默认为 0（系统操作）。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
public class JingWeiMetaObjectHandler implements MetaObjectHandler {

    /** 未登录时默认的用户ID */
    private static final Long DEFAULT_USER_ID = 0L;

    /**
     * 插入时自动填充
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        // 严格模式：只在字段为 null 时填充，避免覆盖业务显式设置的值
        this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "deleted", Boolean.class, false);

        log.debug("插入填充: userId={}, now={}", userId, now);
    }

    /**
     * 更新时自动填充
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        Long userId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        // 更新时覆盖 updatedBy 和 updatedAt（无论原值是否为null）
        this.setFieldValByName("updatedBy", userId, metaObject);
        this.setFieldValByName("updatedAt", now, metaObject);

        log.debug("更新填充: userId={}, now={}", userId, now);
    }

    /**
     * 获取当前用户ID
     * <p>
     * 优先从 UserContext（ThreadLocal）获取，未设置时返回默认值 0。
     * </p>
     *
     * @return 当前用户ID
     */
    private Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        return userId != null ? userId : DEFAULT_USER_ID;
    }
}
