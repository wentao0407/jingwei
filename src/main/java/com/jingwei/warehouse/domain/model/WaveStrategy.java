package com.jingwei.warehouse.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 波次策略枚举 */
@Getter
@RequiredArgsConstructor
public enum WaveStrategy {
    BY_CUSTOMER("BY_CUSTOMER", "按客户"),
    BY_CARRIER("BY_CARRIER", "按物流"),
    BY_ZONE("BY_ZONE", "按库区");

    private final String code;
    private final String label;
}
