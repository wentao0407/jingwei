package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 库存类型枚举
 * <p>
 * 区分成品库存（按SKU管理）和原料库存（按物料管理），
 * 两者使用不同的表但共享变更逻辑。
 * </p>
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum InventoryType {

    /** 成品库存（inventory_sku 表） */
    SKU("SKU", "成品库存"),

    /** 原料库存（inventory_material 表） */
    MATERIAL("MATERIAL", "原料库存");

    private final String code;
    private final String label;
}
