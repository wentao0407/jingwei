package com.jingwei.system.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.system.domain.model.AuditLog;
import com.jingwei.system.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogMapper auditLogMapper;

    @Override
    public int insert(AuditLog auditLog) {
        return auditLogMapper.insert(auditLog);
    }

    @Override
    public IPage<AuditLog> selectPage(IPage<AuditLog> page, Long userId, String module,
                                       String operationType, String startTime, String endTime, String keyword) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<AuditLog>()
                .eq(userId != null, AuditLog::getUserId, userId)
                .eq(module != null && !module.isEmpty(), AuditLog::getModule, module)
                .eq(operationType != null && !operationType.isEmpty(), AuditLog::getOperationType, operationType)
                .ge(startTime != null && !startTime.isEmpty(), AuditLog::getCreatedAt, startTime)
                .le(endTime != null && !endTime.isEmpty(), AuditLog::getCreatedAt, endTime)
                .and(keyword != null && !keyword.isBlank(), w ->
                        w.like(AuditLog::getUsername, keyword)
                                .or()
                                .like(AuditLog::getDescription, keyword))
                .orderByDesc(AuditLog::getCreatedAt);
        return auditLogMapper.selectPage(page, wrapper);
    }
}
