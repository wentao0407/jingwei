package com.jingwei.inventory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建出库单行 DTO
 *
 * @author JingWei
 */
@Data
public class CreateOutboundLineDTO {

    /** 库存类型：SKU/MATERIAL */
    @NotBlank(message = "库存类型不能为空")
    private String inventoryType;

    /** SKU ID（成品时必填） */
    private Long skuId;

    /** 物料ID（原料时必填） */
    private Long materialId;

    /** 批次号 */
    private String batchNo;

    /** 计划出库数量 */
    @NotNull(message = "计划数量不能为空")
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
