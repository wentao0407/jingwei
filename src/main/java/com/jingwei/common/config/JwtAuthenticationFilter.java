package com.jingwei.common.config;

import com.jingwei.common.domain.model.UserContext;
import com.jingwei.system.application.service.MenuApplicationService;
import com.jingwei.system.domain.model.SysUser;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.domain.repository.SysUserRepository;
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
import java.util.List;

/**
 * JWT 认证过滤器
 * <p>
 * 从请求头 Authorization 中提取 JWT Token，验证签名和过期时间后，
 * 再查询用户状态（防止已停用/已删除用户的旧 Token 继续访问），
 * 加载用户权限标识到 SecurityContext，
 * 最后设置 UserContext（供审计字段使用）。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SysUserRepository sysUserRepository;
    private final MenuApplicationService menuApplicationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 从请求头获取 Token
        String token = extractToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            // Token 签名和过期时间校验通过，提取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);

            // 查询用户状态：已停用或已删除的用户的旧 Token 应被拒绝
            // 虽然增加了 DB 查询，但项目并发量低（<20人），可接受
            SysUser user = sysUserRepository.selectById(userId);
            if (user == null || user.getStatus() == UserStatus.INACTIVE) {
                // 用户不存在或已停用，不设置认证上下文，请求会被 Security 拦截返回 401
                log.warn("JWT认证失败: userId={}, 用户不存在或已停用", userId);
                // 清除可能残留的上下文
                SecurityContextHolder.clearContext();
                UserContext.clear();

                // 直接返回 401，不继续过滤器链
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"code\":91003,\"message\":\"用户已停用，无法访问\",\"data\":null}");
                return;
            }

            // 设置 UserContext，供审计字段自动填充使用
            UserContext.setUserId(userId);

            // 加载用户权限标识，构建 Spring Security 的 Authority 列表
            // 权限标识格式：order:sales:create，加上 "PERM_" 前缀以区分角色
            List<SimpleGrantedAuthority> authorities;
            try {
                List<String> permissions = menuApplicationService.getPermissionIdentifiersByUserId(userId);
                authorities = permissions.stream()
                        .map(p -> new SimpleGrantedAuthority("PERM_" + p))
                        .toList();
            } catch (Exception e) {
                // 权限加载失败时降级为空权限，不影响认证
                log.warn("加载用户权限失败: userId={}, error={}", userId, e.getMessage());
                authorities = List.of();
            }

            // 保证至少有一个 authority，Spring Security 需要非空列表
            if (authorities.isEmpty()) {
                authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            }

            // 设置 Spring Security 上下文
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId, null, authorities
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT认证成功: userId={}, username={}, permissions={}", userId, username, authorities.size());
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
