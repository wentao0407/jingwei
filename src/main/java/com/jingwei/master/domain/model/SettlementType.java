package com.jingwei.master.domain.model;

import lombok.Getter;

/**
 * 供应商结算方式枚举
 *
 * @author JingWei
 */
@Getter
public enum SettlementType {

    /** 月结 */
    MONTHLY,

    /** 季结 */
    QUARTERLY,

    /** 货到付款 */
    COD
}
