package com.jingwei.procurement.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 供应商对账单行
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_procurement_statement_line")
public class SupplierStatementLine extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 对账单ID */
    private Long statementId;

    /** 来源ASN ID */
    private Long asnId;

    /** 来源采购订单ID */
    private Long procurementOrderId;

    /** 物料ID */
    private Long materialId;

    /** 检验合格数量 */
    private BigDecimal acceptedQuantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 行金额 = acceptedQuantity × unitPrice */
    private BigDecimal lineAmount;
}
