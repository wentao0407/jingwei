package com.jingwei.approval.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 审批任务实体
 * <p>
 * 对应数据库表 t_sys_approval_task，每次业务单据提交审批时生成。
 * 单人审批模式只生成一条任务；或签模式为每个符合角色的审批人生成一条任务，
 * 任一人审批后其他待办自动取消。
 * </p>
 * <p>
 * 任务状态流转：PENDING → APPROVED / REJECTED / CANCELLED
 * CANCELLED 仅在或签模式下使用，当其他审批人先处理时自动取消。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_sys_approval_task")
public class ApprovalTask extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 业务类型（如 SALES_ORDER、PURCHASE_ORDER） */
    private String businessType;

    /** 业务单据ID */
    private Long businessId;

    /** 业务单据编号 */
    private String businessNo;

    /** 审批模式：SINGLE / OR_SIGN */
    private ApprovalMode approvalMode;

    /** 任务状态：PENDING / APPROVED / REJECTED / CANCELLED */
    private ApprovalTaskStatus status;

    /** 审批人用户ID（待办分配给谁） */
    private Long approverId;

    /** 审批人角色ID */
    private Long approverRoleId;

    /** 审批意见（审批时必填） */
    private String opinion;

    /** 审批时间 */
    private LocalDateTime approvedAt;
}
