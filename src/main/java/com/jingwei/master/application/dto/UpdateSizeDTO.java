package com.jingwei.master.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新尺码请求 DTO
 * <p>
 * 所有字段均为可选，传入的字段才会更新。
 * 注意：如果该尺码组已被 SPU 引用，不允许修改尺码编码（code），
 * 此校验在 DomainService 中执行。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateSizeDTO {

    /** 尺码编码（可选，已被 SPU 引用的尺码组不可修改） */
    @Size(max = 16, message = "尺码编码长度不能超过16个字符")
    private String code;

    /** 尺码名称（可选） */
    @Size(max = 16, message = "尺码名称长度不能超过16个字符")
    private String name;

    /** 排序号（可选） */
    private Integer sortOrder;
}
