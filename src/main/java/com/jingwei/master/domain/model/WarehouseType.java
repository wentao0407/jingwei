package com.jingwei.master.domain.model;

import lombok.Getter;

/**
 * 仓库类型枚举
 * <p>
 * 按仓库存储的货物类型分类，影响出入库策略和库位配置。
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum WarehouseType {

    /** 成品仓 */
    FINISHED_GOODS,

    /** 原料仓 */
    RAW_MATERIAL,

    /** 退货仓 */
    RETURN
}
