package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建尺码组请求 DTO
 * <p>
 * 入参使用 DTO 而非实体类，遵循开发规范。
 * 创建时 status 默认为 ACTIVE，不需要传入。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateSizeGroupDTO {

    /** 尺码组编码（全局唯一，必填） */
    @NotBlank(message = "尺码组编码不能为空")
    @Size(max = 32, message = "尺码组编码长度不能超过32个字符")
    private String code;

    /** 尺码组名称（必填） */
    @NotBlank(message = "尺码组名称不能为空")
    @Size(max = 64, message = "尺码组名称长度不能超过64个字符")
    private String name;

    /** 适用品类：WOMEN/MEN/CHILDREN（必填） */
    @NotBlank(message = "适用品类不能为空")
    private String category;
}
