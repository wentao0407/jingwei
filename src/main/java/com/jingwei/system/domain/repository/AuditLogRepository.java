package com.jingwei.system.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.system.domain.model.AuditLog;

/**
 * 操作日志仓库接口
 * <p>
 * 日志只读，仅提供插入和查询，不提供更新和删除。
 * </p>
 *
 * @author JingWei
 */
public interface AuditLogRepository {

    int insert(AuditLog auditLog);

    IPage<AuditLog> selectPage(IPage<AuditLog> page, Long userId, String module,
                                String operationType, String startTime, String endTime, String keyword);
}
