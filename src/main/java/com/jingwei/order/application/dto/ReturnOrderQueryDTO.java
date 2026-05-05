package com.jingwei.order.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * 退货单分页查询 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ReturnOrderQueryDTO {

    /** 客户ID（可选筛选） */
    private Long customerId;

    /** 状态（可选筛选） */
    private String status;

    /** 页码 */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /** 每页条数 */
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer pageSize = 20;
}
