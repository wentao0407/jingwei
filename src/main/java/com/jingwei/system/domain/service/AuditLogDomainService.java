package com.jingwei.system.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.system.domain.model.AuditLog;
import com.jingwei.system.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 操作日志领域服务
 * <p>
 * 日志只读，仅提供记录和查询，不提供修改和删除。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogDomainService {

    private final AuditLogRepository auditLogRepository;

    /**
     * 记录操作日志
     */
    public void record(AuditLog auditLog) {
        auditLogRepository.insert(auditLog);
    }

    /**
     * 获取仓库引用（供 ApplicationService 分页查询使用）
     */
    public AuditLogRepository getAuditLogRepository() {
        return auditLogRepository;
    }
}
