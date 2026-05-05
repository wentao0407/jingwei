package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 盘点差异状态枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum DiffStatus {

    /** 待审核 */
    PENDING("PENDING", "待审核"),
    /** 已确认（审核通过） */
    CONFIRMED("CONFIRMED", "已确认"),
    /** 已调整（库存已校正） */
    ADJUSTED("ADJUSTED", "已调整");

    private final String code;
    private final String label;
}
