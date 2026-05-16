package com.jingwei.procurement.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 供应商对账单行 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class StatementLineVO {

    private Long id;
    private Long statementId;
    private Long asnId;
    private String asnNo;
    private Long procurementOrderId;
    private String procurementOrderNo;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private BigDecimal acceptedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
}
