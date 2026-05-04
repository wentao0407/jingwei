package com.jingwei.procurement.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 采购订单行 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ProcurementOrderLineVO {

    private Long id;
    private Integer lineNo;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String materialType;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
    private BigDecimal deliveredQuantity;
    private BigDecimal acceptedQuantity;
    private BigDecimal rejectedQuantity;
    private Long mrpResultId;
    private String remark;
}
