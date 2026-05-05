package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 库存预警类型枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum AlertType {

    /** 低库存（可用库存低于阈值） */
    LOW_STOCK("LOW_STOCK", "低库存"),
    /** 超储（可用库存高于阈值） */
    OVERSTOCK("OVERSTOCK", "超储"),
    /** 库龄超期（最后入库距今超过阈值天数） */
    AGING("AGING", "库龄超期");

    private final String code;
    private final String label;
}
