package com.jingwei.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 入库单行实体
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_warehouse_inbound_line")
public class InboundOrderLine extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 入库单ID */
    private Long inboundId;

    /** 行号 */
    private Integer lineNo;

    /** 库存类型：SKU/MATERIAL */
    private InventoryType inventoryType;

    /** SKU ID（成品时使用） */
    private Long skuId;

    /** 物料ID（原料时使用） */
    private Long materialId;

    /** 批次号 */
    private String batchNo;

    /** 计划入库数量 */
    private BigDecimal plannedQty;

    /** 实际入库数量 */
    private BigDecimal actualQty;

    /** 入库库位ID */
    private Long locationId;

    /** 单位成本 */
    private BigDecimal unitCost;

    /** 备注 */
    private String remark;
}
