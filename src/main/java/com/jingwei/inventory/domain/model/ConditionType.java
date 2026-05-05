package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 预警条件类型枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum ConditionType {

    /** 固定值（直接对比库存数量） */
    FIXED_VALUE("FIXED_VALUE", "固定值"),
    /** 可用天数（库存可支撑的天数） */
    DAYS_OF_SUPPLY("DAYS_OF_SUPPLY", "可用天数");

    private final String code;
    private final String label;
}
