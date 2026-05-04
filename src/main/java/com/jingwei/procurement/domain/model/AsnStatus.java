package com.jingwei.procurement.domain.model;

import lombok.Getter;

/**
 * 到货通知单状态枚举
 *
 * @author JingWei
 */
@Getter
public enum AsnStatus {

    /** 待收货 */
    PENDING("待收货"),
    /** 部分收货 */
    PARTIAL_RECEIVED("部分收货"),
    /** 已收货 */
    RECEIVED("已收货"),
    /** 已关闭 */
    CLOSED("已关闭");

    private final String label;

    AsnStatus(String label) {
        this.label = label;
    }
}
