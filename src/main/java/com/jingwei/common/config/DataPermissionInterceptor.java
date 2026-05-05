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

        // 获取 MappedStatement
        MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

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

        // 替换 SQL
        metaObject.setValue("delegate.boundSql.sql", modifiedSql);
        log.debug("数据权限已注入: column={}, values={}, sql={}", permission.column(), allowedValues.size(), modifiedSql);

        return invocation.proceed();
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
     * 当前实现：从 UserContext 获取用户ID，查询其角色的 data_scope。
     * 如果 data_scope=ALL 返回 null（不过滤），否则返回允许的仓库ID列表。
     * </p>
     * <p>
     * 注：具体仓库权限查询需要配合 t_sys_user_warehouse 表使用。
     * 当前为基础设施搭建阶段，返回 null 表示不过滤。
     * 后续在 t_sys_role 增加 data_scope 字段 + t_sys_user_warehouse 表后，
     * 可在此处实现完整的权限查询逻辑。
     * </p>
     */
    private List<Long> getAllowedValues(DataPermission permission) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return null;
        }

        // TODO: 查询用户角色的 data_scope 字段
        // 如果 data_scope = "ALL"，返回 null（不过滤）
        // 如果 data_scope = "WAREHOUSE"，查询 t_sys_user_warehouse 获取允许的仓库ID列表
        // 当前阶段返回 null，表示不注入过滤条件，等权限表完善后在此补充
        return null;
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
