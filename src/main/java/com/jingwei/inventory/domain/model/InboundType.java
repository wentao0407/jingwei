package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 入库类型枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum InboundType {

    /** 采购入库（→质检库存） */
    PURCHASE("PURCHASE", "采购入库"),
    /** 生产完工入库（→可用库存） */
    PRODUCTION("PRODUCTION", "生产入库"),
    /** 客户退货入库（→质检库存） */
    RETURN_SALES("RETURN_SALES", "退货入库"),
    /** 调拨入库（→可用库存） */
    TRANSFER("TRANSFER", "调拨入库");

    private final String code;
    private final String label;

    /**
     * 根据入库类型获取对应的库存操作类型
     */
    public OperationType toOperationType() {
        return switch (this) {
            case PURCHASE -> OperationType.INBOUND_PURCHASE;
            case PRODUCTION -> OperationType.INBOUND_PRODUCTION;
            case RETURN_SALES -> OperationType.INBOUND_RETURN;
            case TRANSFER -> OperationType.INBOUND_PRODUCTION;
        };
    }
}
