package com.jingwei.procurement.domain.model;

import lombok.Getter;

/**
 * BOM 状态枚举
 *
 * @author JingWei
 */
@Getter
public enum BomStatus {

    DRAFT("草稿"),
    APPROVED("已审批"),
    OBSOLETE("已淘汰");

    private final String label;

    BomStatus(String label) {
        this.label = label;
    }
}
