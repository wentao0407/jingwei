package com.jingwei.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 原料库存实体
 * <p>
 * 对应数据库表 t_inventory_material，按物料 + 仓库 + 批次 管理原料库存。
 * 与成品库存的区别：有 supplier_id、procurement_order_id、roll_count 字段，
 * 数量字段使用 DECIMAL（面料按米计量）。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_inventory_material")
public class InventoryMaterial extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 物料ID */
    private Long materialId;

    /** 仓库ID */
    private Long warehouseId;

    /** 库位ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 供应商ID */
    private Long supplierId;

    /** 采购订单ID */
    private Long procurementOrderId;

    /** 可用数量（面料按米，辅料按个） */
    private BigDecimal availableQty;

    /** 锁定数量 */
    private BigDecimal lockedQty;

    /** 质检数量 */
    private BigDecimal qcQty;

    /** 实际库存 = available + locked + qc */
    private BigDecimal totalQty;

    /** 在途数量（冗余） */
    private BigDecimal inTransitQty;

    /** 单位成本 */
    private BigDecimal unitCost;

    /** 卷数（面料专用） */
    private Integer rollCount;

    /** 最后入库日期 */
    private LocalDate lastInboundDate;

    /** 最后出库日期 */
    private LocalDate lastOutboundDate;
}
