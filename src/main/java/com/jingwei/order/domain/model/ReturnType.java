package com.jingwei.order.domain.model;

import lombok.Getter;

/**
 * 退货类型枚举
 * <p>
 * 定义三种退货场景，影响退货流程和质检处理方式。
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum ReturnType {

    /** 客户退货 — 客户收货后因质量/尺码问题退回 */
    CUSTOMER_REJECT("客户退货"),

    /** 物流拒收 — 发货后客户拒收或无法送达 */
    LOGISTICS_REJECT("物流拒收"),

    /** 经销商退货 — 经销商退回未售出的库存（换季退货） */
    DISTRIBUTOR_RETURN("经销商退货");

    /** 中文标签 */
    private final String label;

    ReturnType(String label) {
        this.label = label;
    }
}
