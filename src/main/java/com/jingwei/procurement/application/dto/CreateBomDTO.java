package com.jingwei.procurement.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 创建 BOM DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateBomDTO {

    /** 款式ID */
    @NotNull(message = "款式ID不能为空")
    private Long spuId;

    /** 生效日期 */
    private String effectiveFrom;

    /** 失效日期 */
    private String effectiveTo;

    /** 备注 */
    private String remark;

    /** BOM 行列表 */
    @NotEmpty(message = "BOM行不能为空")
    private List<BomItemCreateDTO> items;
}
