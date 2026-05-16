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
 * 供应商对账单聚合根
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_procurement_statement")
public class SupplierStatement extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 对账单编号 */
    private String statementNo;

    /** 供应商ID */
    private Long supplierId;

    /** 对账期间开始 */
    private LocalDate periodStart;

    /** 对账期间结束 */
    private LocalDate periodEnd;

    /** 对账总金额 */
    private BigDecimal totalAmount;

    /** 状态 */
    private StatementStatus status;

    /** 备注 */
    private String remark;

    /** 对账单行列表（非数据库字段） */
    @TableField(exist = false)
    private List<SupplierStatementLine> lines = new ArrayList<>();
}
