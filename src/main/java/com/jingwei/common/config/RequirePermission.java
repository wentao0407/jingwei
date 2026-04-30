package com.jingwei.common.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * <p>
 * 标注在 Controller 方法上，声明访问该接口所需的权限标识。
 * 权限标识格式：模块:资源:操作，如 order:sales:create。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * &#64;RequirePermission("order:sales:create")
 * &#64;PostMapping("/order/sales/create")
 * public R&lt;Void&gt; createSalesOrder(...) { ... }
 * </pre>
 * </p>
 * <p>
 * 校验逻辑：从 SecurityContext 中获取当前用户的权限列表，
 * 检查是否包含 "PERM_" + permission 标识。
 * </p>
 *
 * @author JingWei
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 权限标识
     *
     * @return 权限标识字符串，如 "order:sales:create"
     */
    String value();
}
