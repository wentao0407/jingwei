package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建尺码请求 DTO
 * <p>
 * 在指定尺码组下新增一个尺码。
 * sortOrder 不传时自动追加到末尾。
 * </p>
 *
 * @author JingWei
 */
@Data
public class CreateSizeDTO {

    /** 尺码编码（组内唯一，必填） */
    @NotBlank(message = "尺码编码不能为空")
    @Size(max = 16, message = "尺码编码长度不能超过16个字符")
    private String code;

    /** 尺码名称（必填） */
    @NotBlank(message = "尺码名称不能为空")
    @Size(max = 16, message = "尺码名称长度不能超过16个字符")
    private String name;

    /** 排序号（可选，不传则追加到末尾） */
    private Integer sortOrder;
}
