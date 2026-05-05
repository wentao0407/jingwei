package com.jingwei.system.interfaces.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * <p>
 * 标注在 Controller 方法上，由 {@link com.jingwei.system.interfaces.aspect.OperateLogAspect}
 * 拦截并自动记录操作日志到 t_sys_audit_log 表。
 * </p>
 * <p>
 * 用法示例：
 * <pre>
 * {@literal @}OperateLog(module = "MASTER", operationType = "CREATE", description = "创建供应商")
 * public R&lt;SupplierVO&gt; createSupplier(...) { ... }
 * </pre>
 * </p>
 *
 * @author JingWei
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperateLog {

    /**
     * 操作模块（如 SYSTEM / MASTER / ORDER / PROCUREMENT / INVENTORY / WAREHOUSE）
     */
    String module();

    /**
     * 操作类型（如 CREATE / UPDATE / DELETE / APPROVE / EXPORT）
     */
    String operationType();

    /**
     * 操作描述（如"创建供应商"、"审批销售订单"）
     */
    String description();
}
