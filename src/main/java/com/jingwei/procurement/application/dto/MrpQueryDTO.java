package com.jingwei.procurement.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * MRP 结果查询 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class MrpQueryDTO {

    /** 当前页码 */
    private Integer current = 1;

    /** 每页条数 */
    private Integer size = 20;

    /** 批次号 */
    private String batchNo;

    /** 状态 */
    private String status;
}
