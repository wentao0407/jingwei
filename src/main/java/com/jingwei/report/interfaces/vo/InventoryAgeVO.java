package com.jingwei.report.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 库龄分析 VO
 *
 * @author JingWei
 */
@Data
public class InventoryAgeVO {

    /** 库存记录ID */
    private Long inventoryId;

    /** 库存类型：SKU/MATERIAL */
    private String inventoryType;

    /** SKU编码 */
    private String skuCode;

    /** 物料编码 */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 款式编码 */
    private String spuCode;

    /** 款式名称 */
    private String spuName;

    /** 颜色名称 */
    private String colorName;

    /** 尺码编码 */
    private String sizeCode;

    /** 仓库名称 */
    private String warehouseName;

    /** 批次号 */
    private String batchNo;

    /** 实际库存 */
    private BigDecimal totalQty;

    /** 库存金额 */
    private BigDecimal totalAmount;

    /** 最后入库日期 */
    private LocalDate lastInboundDate;

    /** 库龄天数（当前日期 - 最后入库日期） */
    private Integer ageDays;

    /** 库龄区间：0-30天 / 31-60天 / 61-90天 / 91-180天 / 180天以上 */
    private String ageRange;

    /** 是否超期预警（默认90天为阈值） */
    private Boolean overdue;
}
