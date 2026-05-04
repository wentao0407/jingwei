package com.jingwei.procurement.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 采购订单行
 * <p>
 * 对应数据库表 t_procurement_order_line。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_procurement_order_line")
public class ProcurementOrderLine extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 采购订单ID */
    private Long orderId;

    /** 行号 */
    private Integer lineNo;

    /** 物料ID */
    private Long materialId;

    /** 物料类型 */
    private String materialType;

    /** 采购数量 */
    private BigDecimal quantity;

    /** 单位 */
    private String unit;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 行金额 = quantity × unitPrice */
    private BigDecimal lineAmount;

    /** 已到货数量 */
    private BigDecimal deliveredQuantity;

    /** 已检验合格数量 */
    private BigDecimal acceptedQuantity;

    /** 检验不合格数量 */
    private BigDecimal rejectedQuantity;

    /** 来源MRP结果ID（可选） */
    private Long mrpResultId;

    /** 备注 */
    private String remark;
}
