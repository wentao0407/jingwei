package com.jingwei.inventory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建入库单 DTO
 *
 * @author JingWei
 */
@Data
public class CreateInboundDTO {

    /** 入库类型：PURCHASE/PRODUCTION/RETURN_SALES/TRANSFER */
    @NotBlank(message = "入库类型不能为空")
    private String inboundType;

    /** 目标仓库ID */
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    /** 来源单据类型 */
    private String sourceType;

    /** 来源单据ID */
    private Long sourceId;

    /** 来源单据编号 */
    private String sourceNo;

    /** 备注 */
    @Size(max = 500, message = "备注不能超过500字")
    private String remark;

    /** 入库单行列表 */
    @NotNull(message = "入库行不能为空")
    @Size(min = 1, message = "至少需要一行入库明细")
    private List<CreateInboundLineDTO> lines;
}
