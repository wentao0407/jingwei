package com.jingwei.warehouse.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 波次状态枚举 */
@Getter
@RequiredArgsConstructor
public enum WaveStatus {
    DRAFT("DRAFT", "草稿"),
    RELEASED("RELEASED", "已释放"),
    PICKING("PICKING", "拣货中"),
    SORTING("SORTING", "分拣复核中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String label;
}
