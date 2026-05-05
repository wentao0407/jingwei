package com.jingwei.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * <p>
 * 配置分页插件、乐观锁插件等全局拦截器。
 * </p>
 *
 * @author JingWei
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器配置
     * <p>
     * 包含分页插件（PostgreSQL方言）和乐观锁插件。
     * </p>
     *
     * @return MybatisPlusInterceptor 拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件，使用 PostgreSQL 方言
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    /**
     * 数据权限拦截器
     * <p>
     * 拦截标注了 @DataPermission 的 Mapper 方法，自动追加数据过滤条件。
     * 需要配合 t_sys_role.data_scope 和 t_sys_user_warehouse 表使用。
     * </p>
     */
    @Bean
    public Interceptor dataPermissionInterceptor() {
        return new DataPermissionInterceptor();
    }
}
