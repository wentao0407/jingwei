package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 颜色项 DTO
 * <p>
 * 创建 SPU 或追加颜色时使用的颜色信息。
 * colorCode 用于 SKU 编码拼接，同一 SPU 内不可重复。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class ColorItemDTO {

    /** 颜色名称（必填，如 黑色） */
    @NotBlank(message = "颜色名称不能为空")
    @Size(max = 32, message = "颜色名称长度不能超过32个字符")
    private String colorName;

    /** 颜色编码（必填，如 BK） */
    @NotBlank(message = "颜色编码不能为空")
    @Size(max = 16, message = "颜色编码长度不能超过16个字符")
    private String colorCode;

    /** 潘通色号（可选） */
    private String pantoneCode;

    /** 对应面料ID（可选） */
    private Long fabricMaterialId;

    /** 颜色款图片URL（可选） */
    private String colorImage;
}
