package com.jingwei.common.config;

import com.jingwei.common.domain.model.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * <p>
 * 从请求头 Authorization 中提取 JWT Token，验证后设置 Spring Security 上下文
 * 和 UserContext（供审计字段使用）。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 从请求头获取 Token
        String token = extractToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            // Token 有效，提取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);

            // 设置 UserContext，供审计字段自动填充使用
            UserContext.setUserId(userId);

            // 设置 Spring Security 上下文
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId, null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT认证成功: userId={}, username={}", userId, username);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清理 UserContext，防止内存泄漏
            UserContext.clear();
        }
    }

    /**
     * 从请求头中提取 Token
     * <p>
     * 支持 Bearer Token 格式：Authorization: Bearer xxx
     * </p>
     *
     * @param request HTTP 请求
     * @return Token 字符串，不存在返回 null
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
