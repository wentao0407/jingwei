package com.jingwei.procurement.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 采购订单查询 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ProcurementOrderQueryDTO {

    private Integer current = 1;
    private Integer size = 20;
    private Long supplierId;
    private String status;
}
