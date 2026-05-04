package com.jingwei.procurement.application.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * MRP 计算请求 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class MrpCalculateDTO {

    /** 生产订单ID列表 */
    @NotEmpty(message = "生产订单ID列表不能为空")
    private List<Long> productionOrderIds;
}
