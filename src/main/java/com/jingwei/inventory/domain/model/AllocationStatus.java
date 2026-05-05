package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 库存预留状态枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum AllocationStatus {

    /** 预留中（尚未出库） */
    ACTIVE("ACTIVE", "预留中"),

    /** 部分出库（已出库一部分） */
    PARTIAL_FULFILLED("PARTIAL_FULFILLED", "部分出库"),

    /** 全部出库（预留已完全兑现） */
    FULFILLED("FULFILLED", "已出库"),

    /** 已释放（订单取消，库存归还） */
    RELEASED("RELEASED", "已释放"),

    /** 已过期（超过有效期未出库，自动释放） */
    EXPIRED("EXPIRED", "已过期");

    private final String code;
    private final String label;
}
