package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 盘点差异原因枚举
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum DiffReason {

    /** 正常误差 */
    NORMAL_ERROR("NORMAL_ERROR", "正常误差"),
    /** 丢失 */
    MISSING("MISSING", "丢失"),
    /** 损坏 */
    DAMAGE("DAMAGE", "损坏"),
    /** 未登记入库 */
    UNREGISTERED_IN("UNREGISTERED_IN", "未登记入库"),
    /** 未登记出库 */
    UNREGISTERED_OUT("UNREGISTERED_OUT", "未登记出库"),
    /** 其他 */
    OTHER("OTHER", "其他");

    private final String code;
    private final String label;
}
