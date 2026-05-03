package com.jingwei.order.domain.model;

import lombok.Getter;

/**
 * 生产订单事件枚举
 * <p>
 * 定义生产订单状态机上可触发的事件，每个事件对应一次状态转移。
 * 事件与状态的合法组合由 {@link com.jingwei.order.domain.service.ProductionOrderStateMachineConfig} 配置，
 * 不在枚举中硬编码映射关系，保证灵活性。
 * </p>
 * <p>
 * 事件分为两类：
 * <ul>
 *   <li>整单事件（orderLevel=true）：作用于主表，如下达、排产</li>
 *   <li>行级事件（orderLevel=false）：作用于行，如裁剪、缝制、后整、完工、入库</li>
 * </ul>
 * 行级事件必须通过 fireLineEvent 触发，不能通过 fireEvent 直接推进主表状态，
 * 否则会破坏"主表状态取所有行最滞后状态"的设计。
 * </p>
 * <p>
 * 事件与状态流转对照表：
 * <ul>
 *   <li>RELEASE — DRAFT → RELEASED（下达生产订单，前提：有BOM和数量）</li>
 *   <li>PLAN — RELEASED → PLANNED（排产完成）</li>
 *   <li>START_CUTTING — PLANNED → CUTTING（开始裁剪，前提：不跳过裁剪）</li>
 *   <li>START_SEWING — PLANNED → SEWING（跳过裁剪直接缝制）/ CUTTING → SEWING（裁剪完成）</li>
 *   <li>START_FINISHING — SEWING → FINISHING（缝制完成，进入后整）</li>
 *   <li>COMPLETE — FINISHING → COMPLETED（生产完工，通知库存准备入库）</li>
 *   <li>STOCK_IN — COMPLETED → STOCKED（入库完成，前提：全部入库，通知销售可发货）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum ProductionOrderEvent {

    /** 下达生产订单（DRAFT → RELEASED，前提：有BOM和数量）— 整单事件 */
    RELEASE("下达生产订单", true),

    /** 排产完成（RELEASED → PLANNED）— 整单事件 */
    PLAN("排产完成", true),

    /** 开始裁剪（PLANNED → CUTTING，前提：不跳过裁剪）— 行级事件 */
    START_CUTTING("开始裁剪", false),

    /** 开始缝制（PLANNED → SEWING 跳过裁剪 / CUTTING → SEWING 裁剪完成）— 行级事件 */
    START_SEWING("开始缝制", false),

    /** 开始后整（SEWING → FINISHING）— 行级事件 */
    START_FINISHING("开始后整", false),

    /** 生产完工（FINISHING → COMPLETED，通知库存准备入库）— 行级事件 */
    COMPLETE("生产完工", false),

    /** 入库完成（COMPLETED → STOCKED，前提：全部入库，通知销售可发货）— 行级事件 */
    STOCK_IN("入库完成", false);

    /** 事件中文标签（用于前端按钮文本、变更日志等） */
    private final String label;

    /** 是否为整单级别事件（true=主表事件，false=行级事件） */
    private final boolean orderLevel;

    ProductionOrderEvent(String label, boolean orderLevel) {
        this.label = label;
        this.orderLevel = orderLevel;
    }
}
