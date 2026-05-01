package com.jingwei.common.domain.model;

/**
 * 通用状态枚举
 * <p>
 * 适用于所有只有"启用/停用"两种状态的业务实体，
 * 如物料分类、物料主数据、编码规则、供应商、客户等。
 * </p>
 * <p>
 * 不适用于有特殊状态语义的实体（如用户状态、订单状态等），
 * 这些实体应定义各自的枚举。
 * </p>
 *
 * @author JingWei
 */
public enum CommonStatus {

    /** 启用 */
    ACTIVE,

    /** 停用 */
    INACTIVE
}
