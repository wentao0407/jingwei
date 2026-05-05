package com.jingwei.common.config;

import com.jingwei.common.annotation.DataPermission;
import com.jingwei.common.domain.model.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * 数据权限拦截器
 * <p>
 * 拦截 MyBatis 查询，检查 Mapper 方法上的 {@link DataPermission} 注解，
 * 自动在 SQL 的 WHERE 子句中追加数据过滤条件。
 * </p>
 * <p>
 * 工作原理：
 * <ol>
 *   <li>拦截 StatementHandler.prepare 方法</li>
 *   <li>检查对应的 MappedStatement 是否有 @DataPermission 注解</li>
 *   <li>解析 SQL，追加 AND column IN (allowedValues) 条件</li>
 *   <li>没有权限范围时（ALL），不追加条件</li>
 * </ol>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DataPermissionInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler handler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(handler);

        // 获取 MappedStatement - 兼容 JDK 17
        MappedStatement ms = getMappedStatement(metaObject, handler);
        if (ms == null) {
            return invocation.proceed();
        }

        // 检查是否有 @DataPermission 注解
        DataPermission permission = getDataPermissionAnnotation(ms);
        if (permission == null) {
            return invocation.proceed();
        }

        // 获取当前用户的权限范围
        List<Long> allowedValues = getAllowedValues(permission);
        if (allowedValues == null || allowedValues.isEmpty()) {
            // ALL 模式或无权限数据，不追加条件
            return invocation.proceed();
        }

        // 修改 SQL，追加过滤条件
        BoundSql boundSql = handler.getBoundSql();
        String originalSql = boundSql.getSql();
        String modifiedSql = appendCondition(originalSql, permission, allowedValues);

        // 替换 SQL - 兼容 JDK 17
        setModifiedSql(metaObject, handler, modifiedSql);
        log.debug("数据权限已注入: column={}, values={}, sql={}", permission.column(), allowedValues.size(), modifiedSql);

        return invocation.proceed();
    }

    /**
     * 获取 MappedStatement - 兼容 JDK 17
     */
    private MappedStatement getMappedStatement(MetaObject metaObject, StatementHandler handler) {
        try {
            // 方式1：直接从 metaObject 获取
            if (metaObject.hasGetter("delegate.mappedStatement")) {
                return (MappedStatement) metaObject.getValue("delegate.mappedStatement");
            }
            // 方式2：通过反射获取
            java.lang.reflect.Field field = handler.getClass().getDeclaredField("delegate");
            field.setAccessible(true);
            Object delegate = field.get(handler);
            if (delegate != null) {
                java.lang.reflect.Field msField = delegate.getClass().getDeclaredField("mappedStatement");
                msField.setAccessible(true);
                return (MappedStatement) msField.get(delegate);
            }
        } catch (Exception e) {
            log.debug("获取 MappedStatement 失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 设置修改后的 SQL - 兼容 JDK 17
     */
    private void setModifiedSql(MetaObject metaObject, StatementHandler handler, String modifiedSql) {
        try {
            // 方式1：直接通过 metaObject 设置
            if (metaObject.hasSetter("delegate.boundSql.sql")) {
                metaObject.setValue("delegate.boundSql.sql", modifiedSql);
                return;
            }
            // 方式2：通过反射设置
            java.lang.reflect.Field field = handler.getClass().getDeclaredField("delegate");
            field.setAccessible(true);
            Object delegate = field.get(handler);
            if (delegate != null) {
                java.lang.reflect.Field bsField = delegate.getClass().getDeclaredField("boundSql");
                bsField.setAccessible(true);
                BoundSql boundSql = (BoundSql) bsField.get(delegate);
                java.lang.reflect.Field sqlField = BoundSql.class.getDeclaredField("sql");
                sqlField.setAccessible(true);
                sqlField.set(boundSql, modifiedSql);
            }
        } catch (Exception e) {
            log.warn("设置修改后的 SQL 失败: {}", e.getMessage());
        }
    }

    /**
     * 获取 Mapper 方法上的 @DataPermission 注解
     */
    private DataPermission getDataPermissionAnnotation(MappedStatement ms) {
        try {
            String id = ms.getId();
            String className = id.substring(0, id.lastIndexOf('.'));
            String methodName = id.substring(id.lastIndexOf('.') + 1);

            Class<?> clazz = Class.forName(className);
            // 查找匹配的方法（不考虑参数类型重载，取第一个匹配的）
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(methodName)) {
                    DataPermission dp = method.getAnnotation(DataPermission.class);
                    if (dp != null) return dp;
                }
            }
        } catch (Exception e) {
            log.debug("获取 DataPermission 注解失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取当前用户允许的数据范围值
     * <p>
     * 从 UserContext 获取用户ID，通过 {@link DataPermissionHelper} 查询其角色的 data_scope。
     * 如果 data_scope=ALL 返回 null（不过滤），否则返回允许的仓库ID列表。
     * </p>
     */
    private List<Long> getAllowedValues(DataPermission permission) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return null;
        }
        return DataPermissionHelper.getAllowedWarehouseIds(userId);
    }

    /**
     * 在 SQL 的 WHERE 子句中追加过滤条件
     *
     * @param originalSql  原始 SQL
     * @param permission   数据权限注解
     * @param allowedValues 允许的值列表
     * @return 修改后的 SQL
     */
    private String appendCondition(String originalSql, DataPermission permission, List<Long> allowedValues) {
        String columnRef = permission.column();
        if (permission.alias() != null && !permission.alias().isEmpty()) {
            columnRef = permission.alias() + "." + permission.column();
        }

        StringBuilder inClause = new StringBuilder(columnRef).append(" IN (");
        for (int i = 0; i < allowedValues.size(); i++) {
            if (i > 0) inClause.append(", ");
            inClause.append(allowedValues.get(i));
        }
        inClause.append(")");

        String condition = inClause.toString();

        // 在 WHERE 子句中追加条件
        String upperSql = originalSql.toUpperCase();
        int whereIndex = upperSql.indexOf("WHERE");
        if (whereIndex >= 0) {
            // 已有 WHERE，在后面追加 AND
            return originalSql.substring(0, whereIndex + 5) + " " + condition + " AND " + originalSql.substring(whereIndex + 6);
        } else {
            // 没有 WHERE，尝试在 ORDER BY / GROUP BY / LIMIT 前插入
            int insertPos = findInsertPosition(upperSql);
            if (insertPos >= 0) {
                return originalSql.substring(0, insertPos) + " WHERE " + condition + " " + originalSql.substring(insertPos);
            }
            // 直接在末尾追加
            return originalSql + " WHERE " + condition;
        }
    }

    /**
     * 查找插入 WHERE 子句的位置（在 ORDER BY / GROUP BY / LIMIT 之前）
     */
    private int findInsertPosition(String upperSql) {
        int pos = -1;
        for (String keyword : List.of(" ORDER BY", " GROUP BY", " LIMIT", " HAVING")) {
            int idx = upperSql.indexOf(keyword);
            if (idx >= 0 && (pos < 0 || idx < pos)) {
                pos = idx;
            }
        }
        return pos;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 无需配置
    }
}
