package com.jingwei.cost.domain.model;

import lombok.Getter;

/**
 * 物料类型枚举
 * <p>
 * 用于领料成本分类归集：面料、辅料、包材。
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum MaterialType {

    /** 面料 */
    MATERIAL("面料"),

    /** 辅料（纽扣、拉链、线等） */
    TRIM("辅料"),

    /** 包材（包装袋、纸箱等） */
    PACKAGING("包材");

    /** 中文标签 */
    private final String label;

    MaterialType(String label) {
        this.label = label;
    }
}
