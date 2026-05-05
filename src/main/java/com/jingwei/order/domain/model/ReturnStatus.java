package com.jingwei.order.domain.model;

import lombok.Getter;

/**
 * 退货单状态枚举
 * <p>
 * 退货单的完整生命周期状态。
 * </p>
 * <p>
 * 状态流转图：
 * <pre>
 *                     ┌──────────┐
 *                     │  DRAFT   │ 草稿
 *                     └────┬─────┘
 *                          │ 提交审批
 *                     ┌────▼──────────┐
 *                     │PENDING_APPROVAL│ 待审批
 *                     └────┬──────────┘
 *                     ┌────┴────┐
 *                     │审批通过   │审批驳回
 *                ┌────▼────┐ ┌──▼──────┐
 *                │APPROVED │ │REJECTED │
 *                │ 已批准    │ │已驳回    │
 *                └────┬────┘ └─────────┘
 *                     │ 仓库收货
 *                ┌────▼─────┐
 *                │RECEIVING │ 收货中
 *                └────┬─────┘
 *                     │ 收货完成 → 质检
 *                ┌────▼─────┐
 *                │    QC    │ 质检中
 *                └────┬─────┘
 *                     │ 质检完成
 *                ┌────▼──────┐
 *                │ COMPLETED │ 已完成
 *                └───────────┘
 * </pre>
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum ReturnStatus {

    /** 草稿 — 初始状态，允许编辑 */
    DRAFT("草稿"),

    /** 待审批 — 已提交，等待审批 */
    PENDING_APPROVAL("待审批"),

    /** 已批准 — 审批通过，等待仓库收货 */
    APPROVED("已批准"),

    /** 已驳回 — 审批不通过（终态） */
    REJECTED("已驳回"),

    /** 收货中 — 仓库正在收货 */
    RECEIVING("收货中"),

    /** 质检中 — 收货完成，等待质检 */
    QC("质检中"),

    /** 已完成 — 质检完成，退货流程结束 */
    COMPLETED("已完成");

    /** 状态中文标签 */
    private final String label;

    ReturnStatus(String label) {
        this.label = label;
    }
}
