package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 预警状态枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum AlertStatus {

    /** 活跃（待处理） */
    ACTIVE("ACTIVE", "待处理"),
    /** 已确认（已查看） */
    ACKNOWLEDGED("ACKNOWLEDGED", "已确认"),
    /** 已解决 */
    RESOLVED("RESOLVED", "已解决");

    private final String code;
    private final String label;
}
