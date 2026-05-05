package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 盘点单状态枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum StocktakingStatus {

    /** 草稿 */
    DRAFT("DRAFT", "草稿"),
    /** 盘点进行中 */
    IN_PROGRESS("IN_PROGRESS", "盘点中"),
    /** 差异审核中 */
    DIFF_REVIEW("DIFF_REVIEW", "差异审核"),
    /** 已完成 */
    COMPLETED("COMPLETED", "已完成"),
    /** 已取消 */
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String label;
}
