package com.jingwei.approval.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 审批任务 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ApprovalTaskVO {

    private Long id;
    private String businessType;
    private Long businessId;
    private String businessNo;
    private String approvalMode;
    private String status;
    private Long approverId;
    private Long approverRoleId;
    private String opinion;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
