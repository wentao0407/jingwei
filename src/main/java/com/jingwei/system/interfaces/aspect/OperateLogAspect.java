package com.jingwei.system.interfaces.aspect;

import com.jingwei.common.domain.model.UserContext;
import com.jingwei.system.domain.model.AuditLog;
import com.jingwei.system.domain.service.AuditLogDomainService;
import com.jingwei.system.interfaces.annotation.OperateLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * 操作日志 AOP 切面
 * <p>
 * 拦截标注了 {@link OperateLog} 注解的 Controller 方法，
 * 自动记录操作人、模块、操作类型、描述、IP 地址、执行耗时等信息到 t_sys_audit_log 表。
 * </p>
 * <p>
 * 设计要点：
 * <ul>
 *   <li>异步写入日志，不阻塞主业务流程</li>
 *   <li>主业务异常时仍然记录日志（记录失败原因）</li>
 *   <li>日志写入失败不影响主业务（catch + warn）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperateLogAspect {

    private final AuditLogDomainService auditLogDomainService;

    /**
     * 环绕通知：拦截 @OperateLog 注解的方法
     *
     * @param joinPoint  切点
     * @param operateLog 注解实例
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperateLog operateLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        String errorMsg = null;

        try {
            // 执行原方法
            Object result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            errorMsg = ex.getMessage();
            throw ex;
        } finally {
            // 异步记录操作日志（不影响主业务）
            long duration = System.currentTimeMillis() - startTime;
            try {
                recordLog(joinPoint, operateLog, duration, errorMsg);
            } catch (Exception e) {
                log.warn("操作日志记录失败: module={}, op={}, error={}",
                        operateLog.module(), operateLog.operationType(), e.getMessage());
            }
        }
    }

    /**
     * 异步记录操作日志
     */
    private void recordLog(ProceedingJoinPoint joinPoint, OperateLog operateLog,
                            long duration, String errorMsg) {
        CompletableFuture.runAsync(() -> {
            try {
                AuditLog auditLog = new AuditLog();

                // 操作人信息
                Long userId = UserContext.getUserId();
                auditLog.setUserId(userId);
                // 从 SecurityContext 获取用户名（JWT Filter 中设置的 principal）
                try {
                    var auth = org.springframework.security.core.context.SecurityContextHolder
                            .getContext().getAuthentication();
                    if (auth != null) {
                        auditLog.setUsername(auth.getName());
                    }
                } catch (Exception e) {
                    log.debug("获取用户名失败: {}", e.getMessage());
                }

                // 注解信息
                auditLog.setModule(operateLog.module());
                auditLog.setOperationType(operateLog.operationType());
                auditLog.setDescription(operateLog.description());

                // 方法信息
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
                String desc = operateLog.description() + " [" + methodName + "]";
                if (errorMsg != null) {
                    desc += " 失败: " + errorMsg;
                }
                auditLog.setDescription(desc);

                // IP 地址
                auditLog.setIpAddress(getClientIp());

                // 时间
                auditLog.setCreatedAt(LocalDateTime.now());

                // 写入
                auditLogDomainService.record(auditLog);
            } catch (Exception e) {
                log.warn("异步记录操作日志失败: {}", e.getMessage());
            }
        });
    }

    /**
     * 获取客户端 IP 地址
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                // 多级代理时取第一个
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        } catch (Exception e) {
            log.debug("获取客户端IP失败: {}", e.getMessage());
        }
        return "unknown";
    }
}
