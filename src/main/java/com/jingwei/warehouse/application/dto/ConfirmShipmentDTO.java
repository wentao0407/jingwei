package com.jingwei.warehouse.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 确认发货 DTO
 *
 * @author JingWei
 */
@Data
public class ConfirmShipmentDTO {

    /** 出库单ID */
    @NotNull(message = "出库单ID不能为空")
    private Long outboundId;

    /** 关联的销售订单ID（可选） */
    private Long salesOrderId;
}
