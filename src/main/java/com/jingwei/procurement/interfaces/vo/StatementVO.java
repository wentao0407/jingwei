package com.jingwei.procurement.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商对账单 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class StatementVO {

    private Long id;
    private String statementNo;
    private Long supplierId;
    private String supplierName;
    private String periodStart;
    private String periodEnd;
    private BigDecimal totalAmount;
    private String status;
    private String statusLabel;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<StatementLineVO> lines;
}
