package com.jingwei.common.config;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限校验切面
 * <p>
 * 拦截标注了 {@link RequirePermission} 注解的 Controller 方法，
 * 从 SecurityContext 中获取当前用户的权限列表，校验是否包含所需权限。
 * 无权限时抛出 BizException，由 GlobalExceptionHandler 统一处理返回 403。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    /**
     * 权限前缀，与 JwtAuthenticationFilter 中加载权限时的前缀一致
     */
    private static final String PERM_PREFIX = "PERM_";

    /**
     * 环绕通知：校验方法上的 @RequirePermission 注解
     *
     * @param joinPoint 切点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(com.jingwei.common.config.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法上的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        String requiredPermission = requirePermission.value();

        // 从 SecurityContext 获取当前用户权限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("权限校验失败: 未认证用户尝试访问需要权限的资源 [{}]", requiredPermission);
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }

        // 收集当前用户的权限标识集合
        Set<String> userPermissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // 校验权限：检查是否包含 "PERM_" + requiredPermission
        String permissionKey = PERM_PREFIX + requiredPermission;
        if (!userPermissions.contains(permissionKey)) {
            log.warn("权限校验失败: userId={}, required={}, owned={}",
                    authentication.getPrincipal(), requiredPermission, userPermissions);
            throw new BizException(ErrorCode.ACCESS_DENIED);
        }

        // 权限校验通过，执行目标方法
        return joinPoint.proceed();
    }
}
