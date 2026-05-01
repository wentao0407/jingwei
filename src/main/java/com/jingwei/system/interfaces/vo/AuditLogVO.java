package com.jingwei.system.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 操作日志 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class AuditLogVO {

    private Long id;
    private Long userId;
    private String username;
    private String operationType;
    private String module;
    private String description;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private LocalDateTime createdAt;
}
