package com.jingwei.warehouse.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 上架状态枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum PutawayLineStatus {

    PENDING("PENDING", "待上架"),
    COMPLETED("COMPLETED", "已上架");

    private final String code;
    private final String label;
}
