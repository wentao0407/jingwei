package com.jingwei.order.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单转化 — 行级入参 DTO
 * <p>
 * 每个对象表示用户选择的一条待转化的销售订单行。
 * skip_cutting 用于标记针织类等无需裁剪的款式。
 * </p>
 *
 * @author JingWei
 */
@Data
public class ConvertLineDTO {

    /** 销售订单行ID（必填） */
    @NotNull(message = "销售订单行ID不能为空")
    private Long salesOrderLineId;

    /** 是否跳过裁剪环节（可选，默认false，针织类产品设为true） */
    private Boolean skipCutting;
}
