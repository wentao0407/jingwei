package com.jingwei.inventory.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 录入实盘数量 DTO
 *
 * @author JingWei
 */
@Data
public class RecordCountDTO {

    /** 盘点单ID */
    @NotNull(message = "盘点单ID不能为空")
    private Long stocktakingId;

    /** 盘点行ID */
    @NotNull(message = "盘点行ID不能为空")
    private Long lineId;

    /** 实盘数量 */
    @NotNull(message = "实盘数量不能为空")
    private BigDecimal actualQty;
}
