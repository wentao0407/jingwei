package com.jingwei.procurement.domain.model;

import lombok.Getter;

/**
 * 对账单状态枚举
 *
 * @author JingWei
 */
@Getter
public enum StatementStatus {

    DRAFT("草稿"),
    CONFIRMED("已确认"),
    DISPUTED("争议中");

    private final String label;

    StatementStatus(String label) {
        this.label = label;
    }
}
