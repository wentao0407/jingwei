package com.jingwei.inventory.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 入库单行 VO
 *
 * @author JingWei
 */
@Data
public class InboundOrderLineVO {
    private Long id;
    private Integer lineNo;
    private String inventoryType;
    private Long skuId;
    private String skuCode;
    private Long materialId;
    private String materialName;
    private String batchNo;
    private BigDecimal plannedQty;
    private BigDecimal actualQty;
    private Long locationId;
    private String locationCode;
    private BigDecimal unitCost;
    private String remark;
}
