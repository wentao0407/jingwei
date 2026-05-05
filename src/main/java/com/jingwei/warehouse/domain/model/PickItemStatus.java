package com.jingwei.warehouse.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 拣货项状态枚举 */
@Getter
@RequiredArgsConstructor
public enum PickItemStatus {
    PICKING("PICKING", "待拣"),
    COMPLETED("COMPLETED", "已拣"),
    SHORT("SHORT", "短拣");

    private final String code;
    private final String label;
}
