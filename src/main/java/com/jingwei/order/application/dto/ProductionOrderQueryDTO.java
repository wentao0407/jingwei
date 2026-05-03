package com.jingwei.order.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 生产订单分页查询 DTO
 *
 * @author JingWei
 */
@Data
public class ProductionOrderQueryDTO {

    /** 状态筛选 */
    private String status;

    /** 订单编号搜索（模糊） */
    private String orderNo;

    /** 计划日期起始 */
    private String planDateStart;

    /** 计划日期结束 */
    private String planDateEnd;

    /** 当前页码 */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Integer current = 1;

    /** 每页大小 */
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小最小为1")
    private Integer size = 10;
}
