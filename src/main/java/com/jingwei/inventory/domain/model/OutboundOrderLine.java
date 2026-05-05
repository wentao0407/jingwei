package com.jingwei.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 出库单行实体
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_warehouse_outbound_line")
public class OutboundOrderLine extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 出库单ID */
    private Long outboundId;

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

    /** 计划出库数量 */
    private BigDecimal plannedQty;

    /** 实际出库数量 */
    private BigDecimal actualQty;

    /** 出库库位ID */
    private Long locationId;

    /** 关联的预留记录ID（销售出库时） */
    private Long allocationId;

    /** 备注 */
    private String remark;
}
