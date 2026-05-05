package com.jingwei.inventory.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 出库单行 VO
 *
 * @author JingWei
 */
@Data
public class OutboundOrderLineVO {
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
    private Long allocationId;
    private String remark;
}
