package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建仓库请求 DTO
 * <p>
 * 仓库编码手动指定（如 WH01），不使用编码规则引擎。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateWarehouseDTO {

    /** 仓库编码（手动指定，如 WH01，全局唯一） */
    @NotBlank(message = "仓库编码不能为空")
    @Size(max = 16, message = "仓库编码长度不能超过16个字符")
    private String code;

    /** 仓库名称（必填） */
    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 64, message = "仓库名称长度不能超过64个字符")
    private String name;

    /** 仓库类型：FINISHED_GOODS/RAW_MATERIAL/RETURN（必填） */
    @NotBlank(message = "仓库类型不能为空")
    private String type;

    /** 地址（可选） */
    private String address;

    /** 仓库管理员ID（可选） */
    private Long managerId;

    /** 备注（可选） */
    private String remark;
}
