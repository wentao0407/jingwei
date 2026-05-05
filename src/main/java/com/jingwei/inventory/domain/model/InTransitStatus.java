package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 在途库存状态枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum InTransitStatus {

    /** 待到货 */
    PENDING("PENDING", "待到货"),

    /** 部分到货 */
    PARTIAL_RECEIVED("PARTIAL_RECEIVED", "部分到货"),

    /** 全部到货 */
    FULLY_RECEIVED("FULLY_RECEIVED", "全部到货");

    private final String code;
    private final String label;
}
