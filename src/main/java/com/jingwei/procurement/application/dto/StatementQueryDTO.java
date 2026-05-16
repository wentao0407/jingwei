package com.jingwei.procurement.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 对账单查询 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class StatementQueryDTO {

    /** 当前页 */
    private Integer current = 1;

    /** 每页大小 */
    private Integer size = 20;

    /** 供应商ID */
    private Long supplierId;

    /** 状态 */
    private String status;
}
