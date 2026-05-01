package com.jingwei.master.domain.model;

/**
 * 供应商资质状态枚举
 * <p>
 * 不合格供应商不可在采购订单中选择。
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum SupplierQualificationStatus {

    /** 合格 */
    QUALIFIED,

    /** 待审 */
    PENDING,

    /** 不合格 */
    DISQUALIFIED
}
