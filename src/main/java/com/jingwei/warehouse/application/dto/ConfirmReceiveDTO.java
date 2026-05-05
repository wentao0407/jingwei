package com.jingwei.warehouse.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 确认收货 DTO
 *
 * @author JingWei
 */
@Data
public class ConfirmReceiveDTO {

    /** 收货行ID */
    @NotNull(message = "收货行ID不能为空")
    private Long receivingLineId;

    /** 实收数量 */
    @NotNull(message = "实收数量不能为空")
    private BigDecimal receivedQty;

    /** 实收卷数（面料专用） */
    private Integer rollCount;
}
