package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import com.jingwei.common.domain.model.CommonStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 客户实体
 * <p>
 * 对应数据库表 t_md_customer，管理客户档案信息。
 * 客户编码由编码规则引擎自动生成，名称不可重复。
 * 客户等级影响定价折扣，信用额度为参考值（本期不做超额拦截）。
 * 停用客户不可创建新销售订单。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_md_customer")
public class Customer extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 客户编码（自动生成，如 CUS-000001） */
    private String code;

    /** 客户名称（全局唯一） */
    private String name;

    /** 简称 */
    private String shortName;

    /** 客户类型：WHOLESALE/RETAIL/ONLINE/FRANCHISE */
    private CustomerType type;

    /** 客户等级：A/B/C/D */
    private CustomerLevel level;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 地址 */
    private String address;

    /** 默认发货地址 */
    private String deliveryAddress;

    /** 结算方式：MONTHLY/QUARTERLY/COD */
    private SettlementType settlementType;

    /** 信用额度（参考值，本期不做超额拦截） */
    private BigDecimal creditLimit;

    /** 状态：ACTIVE/INACTIVE */
    private CommonStatus status;

    /** 备注 */
    private String remark;
}
