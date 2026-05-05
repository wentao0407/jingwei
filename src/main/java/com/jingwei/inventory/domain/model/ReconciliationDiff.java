package com.jingwei.inventory.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 对账差异结果（非持久化值对象）
 * <p>
 * 用于日终对账计算过程中的中间结果，最终转换为 {@link ReconciliationAnomaly} 持久化。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class ReconciliationDiff {

    /** 库存类型 */
    private InventoryType inventoryType;

    /** 库存记录ID */
    private Long inventoryId;

    /** SKU ID（成品时有值） */
    private Long skuId;

    /** 物料 ID（原料时有值） */
    private Long materialId;

    /** 仓库 ID */
    private Long warehouseId;

    /** 期初实际库存 */
    private BigDecimal totalBefore;

    /** 期末实际库存 */
    private BigDecimal totalAfter;

    /** 流水汇总净变动 */
    private BigDecimal opsNetChange;

    /** 差异量 */
    private BigDecimal diffQty;
}
