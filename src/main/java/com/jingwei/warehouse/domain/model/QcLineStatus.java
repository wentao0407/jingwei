package com.jingwei.warehouse.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 收货行质检状态枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum QcLineStatus {

    PENDING("PENDING", "待检验"),
    PASSED("PASSED", "合格"),
    FAILED("FAILED", "不合格"),
    CONCESSION("CONCESSION", "让步接收");

    private final String code;
    private final String label;
}
