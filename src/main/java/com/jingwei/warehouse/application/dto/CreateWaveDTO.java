package com.jingwei.warehouse.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建波次 DTO
 *
 * @author JingWei
 */
@Data
public class CreateWaveDTO {

    /** 仓库ID */
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    /** 波次策略：BY_CUSTOMER/BY_CARRIER/BY_ZONE */
    @NotBlank(message = "波次策略不能为空")
    private String strategy;

    /** 出库单ID列表 */
    @NotEmpty(message = "至少选择一张出库单")
    private List<Long> outboundOrderIds;

    /** 备注 */
    private String remark;
}
