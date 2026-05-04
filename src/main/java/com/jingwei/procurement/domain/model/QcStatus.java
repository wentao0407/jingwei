package com.jingwei.procurement.domain.model;

import lombok.Getter;

/**
 * 检验状态枚举
 *
 * @author JingWei
 */
@Getter
public enum QcStatus {

    /** 待检验 */
    PENDING("待检验"),
    /** 检验合格 */
    PASSED("检验合格"),
    /** 检验不合格 */
    FAILED("检验不合格"),
    /** 让步接收 */
    CONCESSION("让步接收");

    private final String label;

    QcStatus(String label) {
        this.label = label;
    }
}
