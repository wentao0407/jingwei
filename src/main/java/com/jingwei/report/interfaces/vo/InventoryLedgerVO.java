package com.jingwei.report.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存台账 VO
 *
 * @author JingWei
 */
@Data
public class InventoryLedgerVO {

    /** 库存记录ID */
    private Long inventoryId;

    /** 库存类型：SKU/MATERIAL */
    private String inventoryType;

    /** SKU ID（成品时有值） */
    private Long skuId;

    /** SKU编码 */
    private String skuCode;

    /** 物料ID（原料时有值） */
    private Long materialId;

    /** 物料编码 */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 款式ID（成品时有值，通过SKU关联） */
    private Long spuId;

    /** 款式编码 */
    private String spuCode;

    /** 款式名称 */
    private String spuName;

    /** 颜色款ID */
    private Long colorWayId;

    /** 颜色名称 */
    private String colorName;

    /** 尺码ID */
    private Long sizeId;

    /** 尺码编码 */
    private String sizeCode;

    /** 仓库ID */
    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    /** 批次号 */
    private String batchNo;

    /** 可用数量 */
    private BigDecimal availableQty;

    /** 锁定数量 */
    private BigDecimal lockedQty;

    /** 质检数量 */
    private BigDecimal qcQty;

    /** 实际库存（合计） */
    private BigDecimal totalQty;

    /** 单位成本 */
    private BigDecimal unitCost;

    /** 库存金额 = totalQty × unitCost */
    private BigDecimal totalAmount;

    /** 最后入库日期 */
    private LocalDate lastInboundDate;

    /** 最后出库日期 */
    private LocalDate lastOutboundDate;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
