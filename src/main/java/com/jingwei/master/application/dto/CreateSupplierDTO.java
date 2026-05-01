package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建供应商请求 DTO
 * <p>
 * 入参使用 DTO 而非实体类，遵循开发规范。
 * 创建时 code 由编码规则引擎自动生成，status 默认 ACTIVE，
 * qualificationStatus 默认 PENDING，均不需要传入。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateSupplierDTO {

    /** 供应商名称（全局唯一，必填） */
    @NotBlank(message = "供应商名称不能为空")
    @Size(max = 128, message = "供应商名称长度不能超过128个字符")
    private String name;

    /** 简称（可选） */
    @Size(max = 64, message = "简称长度不能超过64个字符")
    private String shortName;

    /** 供应商类型：FABRIC/TRIM/PACKAGING/COMPOSITE（必填） */
    @NotBlank(message = "供应商类型不能为空")
    private String type;

    /** 联系人（可选） */
    @Size(max = 32, message = "联系人长度不能超过32个字符")
    private String contactPerson;

    /** 联系电话（可选） */
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;

    /** 地址（可选） */
    private String address;

    /** 结算方式：MONTHLY/QUARTERLY/COD（可选） */
    private String settlementType;

    /** 平均交货天数（可选） */
    private Integer leadTimeDays;

    /** 备注（可选） */
    private String remark;
}
