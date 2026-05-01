package com.jingwei.master.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新尺码组请求 DTO
 * <p>
 * 所有字段均为可选，传入的字段才会更新。
 * 不允许变更编码（code），编码是尺码组的唯一标识，被 SPU 引用后修改会导致数据不一致。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateSizeGroupDTO {

    /** 尺码组名称（可选） */
    @Size(max = 64, message = "尺码组名称长度不能超过64个字符")
    private String name;

    /** 适用品类：WOMEN/MEN/CHILDREN（可选） */
    private String category;

    /** 状态：ACTIVE/INACTIVE（可选） */
    private String status;
}
