package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 出库单状态枚举
 * <p>
 * 出库流程：DRAFT → CONFIRMED → PICKING → SHIPPED
 * 关键：只有 SHIPPED 状态时才扣减库存（货物实际离开仓库）。
 * </p>
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum OutboundStatus {

    /** 草稿 */
    DRAFT("DRAFT", "草稿"),
    /** 已确认（待拣货） */
    CONFIRMED("CONFIRMED", "已确认"),
    /** 拣货中 */
    PICKING("PICKING", "拣货中"),
    /** 已发货（库存已扣减） */
    SHIPPED("SHIPPED", "已发货"),
    /** 已取消 */
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String label;
}
