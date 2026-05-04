package com.jingwei.procurement.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * BOM 分页查询 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class BomQueryDTO {

    /** 当前页码 */
    private Integer current = 1;

    /** 每页条数 */
    private Integer size = 20;

    /** 款式ID */
    private Long spuId;

    /** 状态：DRAFT/APPROVED/OBSOLETE */
    private String status;
}
