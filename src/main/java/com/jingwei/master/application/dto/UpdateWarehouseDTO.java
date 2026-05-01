package com.jingwei.master.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新仓库请求 DTO
 * <p>
 * 所有字段均为可选，传入的字段才会更新。
 * 不允许变更编码（code）和类型（type）。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateWarehouseDTO {

    /** 仓库名称（可选） */
    @Size(max = 64, message = "仓库名称长度不能超过64个字符")
    private String name;

    /** 地址（可选） */
    private String address;

    /** 仓库管理员ID（可选） */
    private Long managerId;

    /** 备注（可选） */
    private String remark;
}
