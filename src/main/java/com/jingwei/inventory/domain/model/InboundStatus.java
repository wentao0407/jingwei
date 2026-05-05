package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 入库单状态枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum InboundStatus {

    /** 草稿 */
    DRAFT("DRAFT", "草稿"),
    /** 已确认（已入库，库存已变更） */
    CONFIRMED("CONFIRMED", "已确认"),
    /** 已完成 */
    COMPLETED("COMPLETED", "已完成"),
    /** 已取消 */
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String label;
}
