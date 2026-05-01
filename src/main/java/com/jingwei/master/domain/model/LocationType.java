package com.jingwei.master.domain.model;

import lombok.Getter;

/**
 * 库位类型枚举
 * <p>
 * 不同类型的库位用于仓库作业的不同环节。
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum LocationType {

    /** 存储位，长期存放 */
    STORAGE,

    /** 拣货位，高频出入 */
    PICKING,

    /** 暂存位，出入库中间态 */
    STAGING,

    /** 质检位，待检品存放 */
    QC
}
