package com.jingwei.master.domain.model;

/**
 * 供应商类型枚举
 * <p>
 * 按供应商提供的物料类型分类，综合型供应商可同时供应多种物料。
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum SupplierType {

    /** 面料供应商 */
    FABRIC,

    /** 辅料供应商 */
    TRIM,

    /** 包材供应商 */
    PACKAGING,

    /** 综合供应商（可同时供应多种物料） */
    COMPOSITE
}
