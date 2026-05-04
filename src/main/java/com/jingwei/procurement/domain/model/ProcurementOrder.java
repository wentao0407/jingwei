package com.jingwei.procurement.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 采购订单聚合根
 * <p>
 * 对应数据库表 t_procurement_order。
 * 采购订单可从 MRP 结果生成，也可手动创建。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_procurement_order")
public class ProcurementOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 采购订单编号 */
    private String orderNo;

    /** 供应商ID */
    private Long supplierId;

    /** 订单日期 */
    private LocalDate orderDate;

    /** 要求交货日期 */
    private LocalDate expectedDeliveryDate;

    /** 状态 */
    private ProcurementOrderStatus status;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 已付金额 */
    private BigDecimal paidAmount;

    /** 付款状态：UNPAID/PARTIAL/PAID */
    private String paymentStatus;

    /** 来源MRP批次号（可选，手动创建时为空） */
    private String mrpBatchNo;

    /** 备注 */
    private String remark;

    /** 采购订单行列表（非数据库字段） */
    @TableField(exist = false)
    private List<ProcurementOrderLine> lines = new ArrayList<>();
}
