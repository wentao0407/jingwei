package com.jingwei.order.domain.model;

import lombok.Getter;

/**
 * 销售订单状态枚举
 * <p>
 * 定义销售订单的完整生命周期状态，配合 {@link com.jingwei.common.statemachine.StateMachine} 使用。
 * 状态流转规则由 {@link com.jingwei.order.domain.service.SalesOrderStateMachineConfig} 配置。
 * </p>
 * <p>
 * 状态流转图：
 * <pre>
 *                     ┌──────────┐
 *                     │  DRAFT   │ 草稿
 *                     └────┬─────┘
 *                          │ 提交
 *                     ┌────▼─────┐
 *                     │PENDING_AP│ 待审批
 *                     └────┬─────┘
 *                     ┌────┴────┐
 *                     │审批通过   │审批驳回
 *                ┌────▼────┐ ┌──▼──────┐
 *                │CONFIRMED │ │REJECTED │
 *                │ 已确认    │ │已驳回    │
 *                └────┬────┘ └────┬────┘
 *                     │           │修改后重新提交
 *                     │      ┌────▼─────┐
 *                     │      │PENDING_AP│
 *                     │      └─────────┘
 *               ┌─────┘
 *               │          │ 排产
 *               │     ┌────▼─────┐
 *               │     │PRODUCING │ 生产中
 *               │     └────┬─────┘
 *               │          │ 完工入库
 *               │     ┌────▼─────┐
 *               │     │ READY    │ 备货完成
 *               │     └────┬─────┘
 *               │          │ 发货
 *               │     ┌────▼─────┐
 *               │     │ SHIPPED  │ 已发货
 *               │     └────┬─────┘
 *               │          │ 签收
 *               │     ┌────▼─────┐
 *               │     │COMPLETED │ 已完成
 *               │     └──────────┘
 *               │
 *         ┌─────▼─────┐
 *         │ CANCELLED │ 已取消
 *         └───────────┘
 * </pre>
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum SalesOrderStatus {

    /** 草稿 — 初始状态，允许编辑订单内容 */
    DRAFT("草稿"),

    /** 待审批 — 已提交，等待审批引擎处理 */
    PENDING_APPROVAL("待审批"),

    /** 已驳回 — 审批不通过，可修改后重新提交 */
    REJECTED("已驳回"),

    /** 已确认 — 审批通过，触发库存预留 */
    CONFIRMED("已确认"),

    /** 生产中 — 已关联生产订单，正在排产 */
    PRODUCING("生产中"),

    /** 备货完成 — 所有SKU库存已满足，可发货 */
    READY("备货完成"),

    /** 已发货 — 已创建出库单并完成发货 */
    SHIPPED("已发货"),

    /** 已完成 — 客户已签收 */
    COMPLETED("已完成"),

    /** 已取消 — 终态，不可恢复 */
    CANCELLED("已取消");

    /** 状态中文标签（用于前端展示、变更日志等） */
    private final String label;

    SalesOrderStatus(String label) {
        this.label = label;
    }
}
