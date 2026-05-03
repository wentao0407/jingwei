package com.jingwei.approval.domain.model;

import lombok.Getter;

/**
 * 审批任务状态枚举
 * <p>
 * 定义审批任务的生命周期状态：
 * <ul>
 *   <li>PENDING — 待审批：等待审批人处理</li>
 *   <li>APPROVED — 已通过：审批人同意</li>
 *   <li>REJECTED — 已驳回：审批人拒绝</li>
 *   <li>CANCELLED — 已取消：或签模式下其他审批人已处理，本任务自动取消</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum ApprovalTaskStatus {

    /** 待审批 */
    PENDING("待审批"),

    /** 已通过 */
    APPROVED("已通过"),

    /** 已驳回 */
    REJECTED("已驳回"),

    /** 已取消（或签模式下其他审批人已处理） */
    CANCELLED("已取消");

    /** 中文标签 */
    private final String label;

    ApprovalTaskStatus(String label) {
        this.label = label;
    }
}
