package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import com.jingwei.common.domain.model.CommonStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 供应商实体
 * <p>
 * 对应数据库表 t_md_supplier，管理供应商档案信息。
 * 供应商编码由编码规则引擎自动生成，名称不可重复。
 * 不合格供应商不可在采购订单中选择，停用供应商不可创建新采购订单。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_supplier")
public class Supplier extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 供应商编码（自动生成，如 SUP-000001） */
    private String code;

    /** 供应商名称（全局唯一） */
    private String name;

    /** 简称 */
    private String shortName;

    /** 供应商类型：FABRIC/TRIM/PACKAGING/COMPOSITE */
    private SupplierType type;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 地址 */
    private String address;

    /** 结算方式：MONTHLY/QUARTERLY/COD */
    private SettlementType settlementType;

    /** 平均交货天数 */
    private Integer leadTimeDays;

    /** 资质状态：QUALIFIED/PENDING/DISQUALIFIED */
    private SupplierQualificationStatus qualificationStatus;

    /** 状态：ACTIVE/INACTIVE */
    private CommonStatus status;

    /** 备注 */
    private String remark;
}
