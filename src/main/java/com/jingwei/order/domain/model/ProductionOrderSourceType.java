package com.jingwei.order.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 生产订单来源类型枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum ProductionOrderSourceType {

    /** 手动独立创建 */
    MANUAL("MANUAL", "手动创建"),

    /** 从销售订单转化 */
    SALES_ORDER("SALES_ORDER", "销售订单转化");

    private final String code;
    private final String label;

    /**
     * 根据 code 查找枚举，不存在则返回 null
     */
    public static ProductionOrderSourceType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ProductionOrderSourceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
