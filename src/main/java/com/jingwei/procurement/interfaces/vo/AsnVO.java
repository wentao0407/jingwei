package com.jingwei.procurement.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 到货通知单 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class AsnVO {

    private Long id;
    private String asnNo;
    private Long procurementOrderId;
    private String procurementOrderNo;
    private Long supplierId;
    private String supplierName;
    private String expectedArrivalDate;
    private String actualArrivalDate;
    private String status;
    private String statusLabel;
    private Long receiverId;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AsnLineVO> lines;
}
