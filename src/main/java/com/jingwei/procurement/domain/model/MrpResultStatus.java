package com.jingwei.procurement.domain.model;

import lombok.Getter;

/**
 * MRP 结果状态枚举
 *
 * @author JingWei
 */
@Getter
public enum MrpResultStatus {

    PENDING("待审核"),
    APPROVED("已审核"),
    CONVERTED("已转采购单"),
    IGNORED("已忽略"),
    EXPIRED("已过期");

    private final String label;

    MrpResultStatus(String label) {
        this.label = label;
    }
}
