package com.jingwei.master.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新供应商请求 DTO
 * <p>
 * 所有字段均为可选，传入的字段才会更新。
 * 不允许变更编码（code）和类型（type）——
 * 编码被业务单据引用，类型影响采购策略和 BOM 关联，
 * 变更会导致历史数据不一致。如需更换类型，应停用后重新创建。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateSupplierDTO {

    /** 供应商名称（可选，修改时校验唯一性） */
    @Size(max = 128, message = "供应商名称长度不能超过128个字符")
    private String name;

    /** 简称（可选） */
    @Size(max = 64, message = "简称长度不能超过64个字符")
    private String shortName;

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

    /** 资质状态：QUALIFIED/PENDING/DISQUALIFIED（可选） */
    private String qualificationStatus;

    /** 备注（可选） */
    private String remark;
}
