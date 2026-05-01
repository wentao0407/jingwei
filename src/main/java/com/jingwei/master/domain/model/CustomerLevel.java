package com.jingwei.master.domain.model;

import lombok.Getter;

/**
 * 客户等级枚举
 * <p>
 * 客户等级影响定价折扣（A客户95折、B客户9折等，可在价格表中配置）。
 * 等级为参考值，不参与本期业务拦截。
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum CustomerLevel {

    /** A级客户（最高等级） */
    A,

    /** B级客户 */
    B,

    /** C级客户 */
    C,

    /** D级客户（最低等级） */
    D
}
