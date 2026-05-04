package com.jingwei.procurement.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 采购订单状态机事件触发 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class FireProcurementOrderEventDTO {

    /** 订单ID */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /** 事件编码 */
    @NotBlank(message = "事件编码不能为空")
    private String event;
}
