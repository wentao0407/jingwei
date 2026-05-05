package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 盘点类型枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum StocktakingType {

    /** 全面盘点（仓库所有库存） */
    FULL("FULL", "全面盘点"),
    /** 循环盘点（每天一部分） */
    CYCLE("CYCLE", "循环盘点"),
    /** 抽盘（随机抽取） */
    SAMPLE("SAMPLE", "抽盘"),
    /** 动盘（有出入库操作的SKU当天盘点） */
    DYNAMIC("DYNAMIC", "动盘");

    private final String code;
    private final String label;
}
