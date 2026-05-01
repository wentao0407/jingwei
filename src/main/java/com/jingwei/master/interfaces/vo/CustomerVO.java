package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户响应 VO
 * <p>
 * 返回给前端的客户数据，包含客户全部业务字段。
 * 枚举字段以字符串形式返回（如 "WHOLESALE"、"A"），
 * 前端根据枚举值做映射展示。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class CustomerVO {

    /** 客户ID */
    private Long id;

    /** 客户编码 */
    private String code;

    /** 客户名称 */
    private String name;

    /** 简称 */
    private String shortName;

    /** 客户类型：WHOLESALE/RETAIL/ONLINE/FRANCHISE */
    private String type;

    /** 客户等级：A/B/C/D */
    private String level;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 地址 */
    private String address;

    /** 默认发货地址 */
    private String deliveryAddress;

    /** 结算方式：MONTHLY/QUARTERLY/COD */
    private String settlementType;

    /** 信用额度 */
    private BigDecimal creditLimit;

    /** 状态：ACTIVE/INACTIVE */
    private String status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
