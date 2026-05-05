package com.jingwei.report.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出入库流水 VO
 *
 * @author JingWei
 */
@Data
public class OperationFlowVO {

    /** 操作记录ID */
    private Long id;

    /** 操作单号 */
    private String operationNo;

    /** 操作类型 code */
    private String operationType;

    /** 操作类型名称 */
    private String operationTypeLabel;

    /** 库存类型：SKU/MATERIAL */
    private String inventoryType;

    /** SKU编码（成品时有值） */
    private String skuCode;

    /** 物料编码（原料时有值） */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 仓库名称 */
    private String warehouseName;

    /** 批次号 */
    private String batchNo;

    /** 操作数量 */
    private BigDecimal quantity;

    /** 操作前实际库存 */
    private BigDecimal totalBefore;

    /** 操作后实际库存 */
    private BigDecimal totalAfter;

    /** 变动数量（入库正数，出库负数） */
    private BigDecimal changeQty;

    /** 单位成本 */
    private BigDecimal unitCost;

    /** 成本金额 */
    private BigDecimal costAmount;

    /** 来源单据类型 */
    private String sourceType;

    /** 来源单据编号 */
    private String sourceNo;

    /** 操作人ID */
    private Long operatorId;

    /** 操作时间 */
    private LocalDateTime operatedAt;

    /** 备注 */
    private String remark;
}
