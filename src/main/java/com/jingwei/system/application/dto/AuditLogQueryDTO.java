package com.jingwei.system.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 操作日志分页查询 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class AuditLogQueryDTO {

    private Long current = 1L;
    private Long size = 20L;

    /** 操作人ID */
    private Long userId;

    /** 操作模块：SYSTEM/MASTER/ORDER/PROCUREMENT/INVENTORY/WAREHOUSE */
    private String module;

    /** 操作类型：CREATE/UPDATE/DELETE/LOGIN/OTHER */
    private String operationType;

    /** 开始时间（ISO格式，如 2026-01-01T00:00:00） */
    private String startTime;

    /** 结束时间 */
    private String endTime;

    /** 关键词（搜索用户名或操作描述） */
    private String keyword;
}
