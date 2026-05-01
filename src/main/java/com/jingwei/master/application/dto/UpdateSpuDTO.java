package com.jingwei.master.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新款式请求 DTO
 * <p>
 * 不允许变更编码和尺码组，这些是 SPU 的核心标识。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateSpuDTO {

    /** 款式名称（可选） */
    @Size(max = 128, message = "款式名称长度不能超过128个字符")
    private String name;

    /** 季节ID（可选） */
    private Long seasonId;

    /** 品类ID（可选） */
    private Long categoryId;

    /** 品牌ID（可选） */
    private Long brandId;

    /** 款式图URL（可选） */
    private String designImage;

    /** 状态：DRAFT/ACTIVE/INACTIVE（可选） */
    private String status;

    /** 备注（可选） */
    private String remark;
}
