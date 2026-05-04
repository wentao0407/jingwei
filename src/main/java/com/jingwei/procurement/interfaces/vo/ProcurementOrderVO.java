package com.jingwei.procurement.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购订单 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ProcurementOrderVO {

    private Long id;
    private String orderNo;
    private Long supplierId;
    private String supplierName;
    private String orderDate;
    private String expectedDeliveryDate;
    private String status;
    private String statusLabel;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private String paymentStatus;
    private String mrpBatchNo;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProcurementOrderLineVO> lines;
}
