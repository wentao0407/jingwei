package com.jingwei.warehouse.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 确认上架 DTO
 *
 * @author JingWei
 */
@Data
public class ConfirmPutawayDTO {

    /** 收货行ID */
    @NotNull(message = "收货行ID不能为空")
    private Long receivingLineId;

    /** 上架库位ID */
    @NotNull(message = "库位ID不能为空")
    private Long locationId;
}
