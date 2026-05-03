package com.jingwei.approval.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批配置 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ApprovalConfigVO {

    private Long id;
    private String businessType;
    private String configName;
    private String approvalMode;
    private List<Long> approverRoleIds;
    private Boolean enabled;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
}
