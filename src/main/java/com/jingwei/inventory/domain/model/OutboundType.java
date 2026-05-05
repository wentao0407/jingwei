package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 出库类型枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum OutboundType {

    /** 销售出库（从锁定库存扣减） */
    SALES("SALES", "销售出库"),
    /** 领料出库（从可用库存扣减） */
    MATERIAL("MATERIAL", "领料出库"),
    /** 调拨出库（从可用库存扣减） */
    TRANSFER("TRANSFER", "调拨出库"),
    /** 采购退货出库（从质检库存扣减） */
    RETURN_PURCHASE("RETURN_PURCHASE", "采购退货");

    private final String code;
    private final String label;

    /**
     * 根据出库类型获取对应的库存操作类型
     */
    public OperationType toOperationType() {
        return switch (this) {
            case SALES -> OperationType.OUTBOUND_SALES;
            case MATERIAL -> OperationType.OUTBOUND_MATERIAL;
            case TRANSFER -> OperationType.OUTBOUND_MATERIAL;
            case RETURN_PURCHASE -> OperationType.QC_FAIL;
        };
    }
}
