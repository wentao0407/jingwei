package com.jingwei.system.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 * <p>
 * 对应数据库表 t_sys_audit_log，记录用户操作审计日志。
 * 日志只读，不可修改和删除，因此不继承 BaseEntity（无软删除和乐观锁）。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_sys_audit_log")
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

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
