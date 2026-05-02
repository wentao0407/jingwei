package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 创建款式请求 DTO
 * <p>
 * 创建 SPU 时必须选择尺码组和至少一个颜色，
 * 系统自动按颜色×尺码交叉生成 SKU。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateSpuDTO {

    /** 款式名称（必填） */
    @NotBlank(message = "款式名称不能为空")
    @Size(max = 128, message = "款式名称长度不能超过128个字符")
    private String name;

    /** 季节ID（可选） */
    private Long seasonId;

    /** 品类ID（可选） */
    private Long categoryId;

    /** 品牌ID（可选） */
    private Long brandId;

    /** 尺码组ID（必填，创建后不可更换） */
    @NotNull(message = "必须选择尺码组")
    private Long sizeGroupId;

    /** 款式图URL（可选） */
    private String designImage;

    /** 备注（可选） */
    private String remark;

    /** 颜色列表（必填，至少一个颜色） */
    @NotEmpty(message = "至少选择一个颜色")
    private List<ColorItemDTO> colors;
}
