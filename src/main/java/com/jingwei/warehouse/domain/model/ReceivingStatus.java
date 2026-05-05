package com.jingwei.warehouse.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 收货状态枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum ReceivingStatus {

    IN_PROGRESS("IN_PROGRESS", "收货中"),
    COMPLETED("COMPLETED", "已完成");

    private final String code;
    private final String label;
}
