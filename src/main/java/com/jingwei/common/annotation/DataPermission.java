package com.jingwei.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * <p>
 * 标注在 Mapper 方法上，由 {@link com.jingwei.common.config.DataPermissionInterceptor}
 * 在 SQL 执行前自动追加过滤条件。
 * </p>
 * <p>
 * 使用方式：
 * <pre>
 * // 按仓库过滤
 * {@code @DataPermission(alias = "inv", column = "warehouse_id")}
 * List&lt;InventorySku&gt; selectByCondition(...);
 * </pre>
 * </p>
 *
 * @author JingWei
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 表别名（SQL 中的表别名前缀，如 "inv"）
     * <p>
     * 为空时直接使用 column，不加别名前缀。
     * </p>
     */
    String alias() default "";

    /**
     * 需要过滤的列名（如 "warehouse_id"）
     */
    String column() default "warehouse_id";

    /**
     * 数据权限范围类型字段（对应 t_sys_role.data_scope）
     * <p>
     * ALL = 不过滤，WAREHOUSE = 按用户仓库权限过滤
     * </p>
     */
    String scopeType() default "WAREHOUSE";
}
