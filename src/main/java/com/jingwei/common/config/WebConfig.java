package com.jingwei.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 注册自定义拦截器，如幂等令牌拦截器。
 * </p>
 *
 * @author JingWei
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final IdempotencyInterceptor idempotencyInterceptor;

    /**
     * 注册拦截器
     * <p>
     * 幂等拦截器优先级高于业务拦截器，确保重复请求在进入 Controller 之前就被拦截。
     * </p>
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(idempotencyInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/actuator/**");
    }
}
