package com.jingwei.order.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 编辑生产订单 DTO
 *
 * @author JingWei
 */
@Data
public class UpdateProductionOrderDTO {

    /** 计划生产日期 */
    private String planDate;

    /** 要求完工日期 */
    private String deadlineDate;

    /** 车间ID */
    private Long workshopId;

    /** 备注 */
    @Size(max = 500, message = "备注不能超过500字")
    private String remark;

    /** 订单行列表（全量替换） */
    @NotNull(message = "订单行不能为空")
    @Size(min = 1, message = "订单至少需要一行明细")
    private List<ProductionOrderLineCreateDTO> lines;
}
