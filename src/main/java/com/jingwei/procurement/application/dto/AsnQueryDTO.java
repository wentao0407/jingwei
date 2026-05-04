package com.jingwei.procurement.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 到货通知单查询 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class AsnQueryDTO {

    private Integer current = 1;
    private Integer size = 20;
    private Long procurementOrderId;
    private String status;
}
