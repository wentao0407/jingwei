package com.jingwei.master.domain.model;

import lombok.Getter;

/**
 * 库位状态枚举
 * <p>
 * FROZEN 状态的库位不可进行出入库操作，通常在盘点期间冻结。
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum LocationStatus {

    /** 启用 */
    ACTIVE,

    /** 停用 */
    INACTIVE,

    /** 冻结（盘点期间） */
    FROZEN
}
