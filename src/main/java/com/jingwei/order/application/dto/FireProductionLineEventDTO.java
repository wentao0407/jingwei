package com.jingwei.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 生产订单行状态机事件触发 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class FireProductionLineEventDTO {

    /** 订单ID */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 行ID */
    @NotNull(message = "行ID不能为空")
    private Long lineId;

    /** 事件编码（START_CUTTING/START_SEWING/START_FINISHING/COMPLETE/STOCK_IN） */
    @NotBlank(message = "事件编码不能为空")
    private String event;
}
