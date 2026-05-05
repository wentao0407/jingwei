package com.jingwei.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 盘点行实体
 * <p>
 * 每行对应一条库存记录（SKU/物料+仓库+库位+批次）。
 * 盲盘模式下，查询盘点行时 system_qty 字段置空不返回前端。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_inventory_stocktaking_line")
public class StocktakingLine extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 盘点单ID */
    private Long stocktakingId;

    /** 库存类型：SKU/MATERIAL */
    private InventoryType inventoryType;

    /** SKU ID（成品时使用） */
    private Long skuId;

    /** 物料ID（原料时使用） */
    private Long materialId;

    /** 仓库ID */
    private Long warehouseId;

    /** 库位ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 系统数量（盲盘时盘点阶段不展示） */
    private BigDecimal systemQty;

    /** 实盘数量（盘点人填写） */
    private BigDecimal actualQty;

    /** 差异数量 = actual_qty - system_qty */
    private BigDecimal diffQty;

    /** 差异状态 */
    private DiffStatus diffStatus;

    /** 差异原因 */
    private DiffReason diffReason;

    /** 调整后数量（审核确认后填写） */
    private BigDecimal adjustedQty;

    /** 第一轮盘点人ID */
    private Long countBy1;

    /** 第一轮盘点时间 */
    private LocalDateTime countAt1;

    /** 第二轮盘点人ID（复盘时） */
    private Long countBy2;

    /** 第二轮盘点时间 */
    private LocalDateTime countAt2;

    /** 是否需要复盘（差异率超过阈值） */
    private Boolean needRecheck;

    /** 备注 */
    private String remark;
}
