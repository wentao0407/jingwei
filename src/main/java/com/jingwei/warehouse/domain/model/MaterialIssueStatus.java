package com.jingwei.warehouse.domain.model;

import lombok.Getter;

/**
 * 领料单状态枚举
 *
 * @author JingWei
 */
@Getter
public enum MaterialIssueStatus {

    DRAFT("草稿"),
    CONFIRMED("已确认"),
    CANCELLED("已取消");

    private final String label;

    MaterialIssueStatus(String label) {
        this.label = label;
    }
}
