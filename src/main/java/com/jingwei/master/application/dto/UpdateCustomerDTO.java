package com.jingwei.master.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 更新客户请求 DTO
 * <p>
 * 所有字段均为可选，传入的字段才会更新。
 * 不允许变更编码（code）和类型（type）——
 * 编码被业务单据引用，类型影响定价策略和销售渠道，
 * 变更会导致历史数据不一致。如需更换类型，应停用后重新创建。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateCustomerDTO {

    /** 客户名称（可选，修改时校验唯一性） */
    @Size(max = 128, message = "客户名称长度不能超过128个字符")
    private String name;

    /** 简称（可选） */
    @Size(max = 64, message = "简称长度不能超过64个字符")
    private String shortName;

    /** 客户等级：A/B/C/D（可选） */
    private String level;

    /** 联系人（可选） */
    @Size(max = 32, message = "联系人长度不能超过32个字符")
    private String contactPerson;

    /** 联系电话（可选） */
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;

    /** 地址（可选） */
    private String address;

    /** 默认发货地址（可选） */
    private String deliveryAddress;

    /** 结算方式：MONTHLY/QUARTERLY/COD（可选） */
    private String settlementType;

    /** 信用额度（可选，参考值） */
    private BigDecimal creditLimit;

    /** 备注（可选） */
    private String remark;
}
