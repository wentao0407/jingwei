package com.jingwei.warehouse.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 拣货单状态枚举 */
@Getter
@RequiredArgsConstructor
public enum PickListStatus {
    PICKING("PICKING", "拣货中"),
    COMPLETED("COMPLETED", "已完成"),
    DISCREPANCY("DISCREPANCY", "有差异");

    private final String code;
    private final String label;
}
