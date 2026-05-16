package com.jingwei.inventory.domain.model;

import lombok.Getter;

/**
 * 调拨单状态枚举
 *
 * @author JingWei
 */
@Getter
public enum TransferStatus {

    DRAFT("草稿"),
    CONFIRMED("已确认"),
    IN_TRANSIT("调拨在途"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String label;

    TransferStatus(String label) {
        this.label = label;
    }
}
