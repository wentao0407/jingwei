package com.jingwei.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 幂等响应缓存过滤器
 * <p>
 * 在请求进入 Controller 之前，将 HttpServletResponse 包装为
 * {@link CachedBodyHttpServletResponseWrapper}，使响应体可被读取和缓存。
 * 配合 {@link IdempotencyInterceptor} 使用。
 * </p>
 * <p>
 * 仅在请求携带 X-Idempotency-Key 时才包装响应，避免不必要的性能开销。
 * </p>
 *
 * @author JingWei
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class IdempotencyFilter extends OncePerRequestFilter {

    /**
     * HTTP 请求头名称：客户端传入的幂等令牌。
     * 与 IdempotencyInterceptor.IDEMPOTENCY_HEADER 保持一致，
     * Filter 层负责将 header 值存入 request attribute 供 Interceptor 读取。
     */
    private static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String key = request.getHeader(IDEMPOTENCY_HEADER);
        if (key != null && !key.isBlank()) {
            // 包装响应，使响应体可被缓存
            CachedBodyHttpServletResponseWrapper wrappedResponse = new CachedBodyHttpServletResponseWrapper(response);
            filterChain.doFilter(request, wrappedResponse);
            // 将缓存的响应体刷入原始响应
            wrappedResponse.flushBuffer();
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
