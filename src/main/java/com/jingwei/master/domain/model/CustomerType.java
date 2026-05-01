package com.jingwei.master.domain.model;

import lombok.Getter;

/**
 * 客户类型枚举
 * <p>
 * 按客户的经营模式分类，影响定价策略和发货方式。
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum CustomerType {

    /** 批发客户 */
    WHOLESALE,

    /** 零售客户 */
    RETAIL,

    /** 线上客户（电商） */
    ONLINE,

    /** 加盟客户 */
    FRANCHISE
}
