package com.jingwei.procurement.domain.model;

import lombok.Getter;

/**
 * BOM 消耗类型枚举
 * <ul>
 *   <li>FIXED_PER_PIECE — 每件固定用量（如纽扣8颗/件）</li>
 *   <li>SIZE_DEPENDENT — 用量随尺码变化（如面料）</li>
 *   <li>PER_ORDER — 按订单整体用量（如唛头1套/款）</li>
 * </ul>
 *
 * @author JingWei
 */
@Getter
public enum ConsumptionType {

    FIXED_PER_PIECE("每件固定"),
    SIZE_DEPENDENT("尺码相关"),
    PER_ORDER("按订单");

    private final String label;

    ConsumptionType(String label) {
        this.label = label;
    }
}
