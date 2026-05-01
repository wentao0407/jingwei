package com.jingwei.master.domain.model;

/**
 * 物料类型枚举
 * <p>
 * 仅包含面料、辅料、包材三种类型。
 * 成品不通过 md_material 表管理，而是走 SPU/SKU 模型，
 * 因此此枚举不包含 PRODUCT。
 * </p>
 *
 * @author JingWei
 */
public enum MaterialType {

    /** 面料 */
    FABRIC,

    /** 辅料 */
    TRIM,

    /** 包材 */
    PACKAGING
}
