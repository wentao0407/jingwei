package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 供应商响应 VO
 * <p>
 * 返回给前端的供应商数据，包含供应商全部业务字段。
 * 枚举字段以字符串形式返回（如 "FABRIC"、"QUALIFIED"），
 * 前端根据枚举值做映射展示。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class SupplierVO {

    /** 供应商ID */
    private Long id;

    /** 供应商编码 */
    private String code;

    /** 供应商名称 */
    private String name;

    /** 简称 */
    private String shortName;

    /** 供应商类型：FABRIC/TRIM/PACKAGING/COMPOSITE */
    private String type;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 地址 */
    private String address;

    /** 结算方式：MONTHLY/QUARTERLY/COD */
    private String settlementType;

    /** 平均交货天数 */
    private Integer leadTimeDays;

    /** 资质状态：QUALIFIED/PENDING/DISQUALIFIED */
    private String qualificationStatus;

    /** 状态：ACTIVE/INACTIVE */
    private String status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
