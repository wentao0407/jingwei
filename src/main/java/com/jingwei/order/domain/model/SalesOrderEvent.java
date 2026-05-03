package com.jingwei.order.domain.model;

import lombok.Getter;

/**
 * 销售订单事件枚举
 * <p>
 * 定义销售订单状态机上可触发的事件，每个事件对应一次状态转移。
 * 事件与状态的合法组合由 {@link com.jingwei.order.domain.service.SalesOrderStateMachineConfig} 配置，
 * 不在枚举中硬编码映射关系，保证灵活性。
 * </p>
 * <p>
 * 事件与状态流转对照表：
 * <ul>
 *   <li>SUBMIT — DRAFT → PENDING_APPROVAL（提交审批）</li>
 *   <li>APPROVE — PENDING_APPROVAL → CONFIRMED（审批通过，触发库存预留）</li>
 *   <li>REJECT — PENDING_APPROVAL → REJECTED（审批驳回）</li>
 *   <li>RESUBMIT — REJECTED → PENDING_APPROVAL（修改后重新提交）</li>
 *   <li>START_PRODUCE — CONFIRMED → PRODUCING（开始排产，前提：已关联生产订单）</li>
 *   <li>READY_STOCK — PRODUCING → READY（备货完成，前提：所有SKU库存满足）</li>
 *   <li>SHIP — READY → SHIPPED / PRODUCING → SHIPPED（发货）</li>
 *   <li>SIGN_OFF — SHIPPED → COMPLETED（确认签收）</li>
 *   <li>CANCEL — DRAFT → CANCELLED / CONFIRMED → CANCELLED（取消）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum SalesOrderEvent {

    /** 提交审批（DRAFT → PENDING_APPROVAL） */
    SUBMIT("提交审批"),

    /** 审批通过（PENDING_APPROVAL → CONFIRMED，触发库存预留） */
    APPROVE("审批通过"),

    /** 审批驳回（PENDING_APPROVAL → REJECTED） */
    REJECT("审批驳回"),

    /** 修改后重新提交（REJECTED → PENDING_APPROVAL） */
    RESUBMIT("重新提交"),

    /** 开始排产（CONFIRMED → PRODUCING，前提：已关联生产订单） */
    START_PRODUCE("开始排产"),

    /** 备货完成（PRODUCING → READY，前提：所有SKU库存满足） */
    READY_STOCK("备货完成"),

    /** 发货（READY → SHIPPED / PRODUCING → SHIPPED） */
    SHIP("发货"),

    /** 确认签收（SHIPPED → COMPLETED） */
    SIGN_OFF("确认签收"),

    /** 取消订单（DRAFT → CANCELLED / CONFIRMED → CANCELLED） */
    CANCEL("取消订单");

    /** 事件中文标签（用于前端按钮文本、变更日志等） */
    private final String label;

    SalesOrderEvent(String label) {
        this.label = label;
    }
}
