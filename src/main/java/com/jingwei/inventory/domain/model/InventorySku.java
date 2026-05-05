package com.jingwei.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 成品库存实体
 * <p>
 * 对应数据库表 t_inventory_sku，按 SKU + 仓库 + 批次 管理成品库存。
 * 唯一约束：(sku_id, warehouse_id, batch_no)
 * </p>
 * <p>
 * 四类库存关系：total_qty = available_qty + locked_qty + qc_qty
 * 在途库存不计入实际库存（in_transit_qty 为冗余字段）。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_inventory_sku")
public class InventorySku extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** SKU ID */
    private Long skuId;

    /** 仓库ID */
    private Long warehouseId;

    /** 库位ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 可用数量 */
    private Integer availableQty;

    /** 锁定数量 */
    private Integer lockedQty;

    /** 质检数量 */
    private Integer qcQty;

    /** 实际库存 = available + locked + qc */
    private Integer totalQty;

    /** 在途数量（冗余） */
    private Integer inTransitQty;

    /** 单位成本（加权平均法） */
    private BigDecimal unitCost;

    /** 最后入库日期 */
    private LocalDate lastInboundDate;

    /** 最后出库日期 */
    private LocalDate lastOutboundDate;
}
