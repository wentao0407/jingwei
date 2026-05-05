package com.jingwei.warehouse.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 确认拣货 DTO
 *
 * @author JingWei
 */
@Data
public class ConfirmPickDTO {

    /** 拣货项ID */
    @NotNull(message = "拣货项ID不能为空")
    private Long pickItemId;

    /** 实际拣货数量 */
    @NotNull(message = "实际拣货数量不能为空")
    private BigDecimal actualQty;
}
