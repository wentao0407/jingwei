package com.jingwei.procurement.domain.model;

import lombok.Getter;

/**
 * 采购订单状态枚举
 *
 * @author JingWei
 */
@Getter
public enum ProcurementOrderStatus {

    DRAFT("草稿"),
    PENDING_APPROVAL("待审批"),
    APPROVED("已审批"),
    REJECTED("已驳回"),
    ISSUED("已下发"),
    RECEIVING("到货中"),
    COMPLETED("已完成");

    private final String label;

    ProcurementOrderStatus(String label) {
        this.label = label;
    }
}
