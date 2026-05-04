package com.jingwei.procurement.application.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 编辑 BOM DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateBomDTO {

    /** 生效日期 */
    private String effectiveFrom;

    /** 失效日期 */
    private String effectiveTo;

    /** 备注 */
    private String remark;

    /** BOM 行列表（全量替换） */
    @NotEmpty(message = "BOM行不能为空")
    private List<BomItemCreateDTO> items;
}
