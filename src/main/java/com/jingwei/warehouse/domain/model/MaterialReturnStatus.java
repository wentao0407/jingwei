package com.jingwei.warehouse.domain.model;

import lombok.Getter;

@Getter
public enum MaterialReturnStatus {
    DRAFT("草稿"),
    CONFIRMED("已确认"),
    CANCELLED("已取消");

    private final String label;
    MaterialReturnStatus(String label) { this.label = label; }
}
