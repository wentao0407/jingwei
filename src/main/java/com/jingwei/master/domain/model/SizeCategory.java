package com.jingwei.master.domain.model;

/**
 * 尺码适用品类枚举
 * <p>
 * 标识尺码组适用的品类范围，不同品类的尺码体系不同。
 * <ul>
 *   <li>WOMEN — 女装（如 XS/S/M/L/XL/XXL 或裤装码 25-31）</li>
 *   <li>MEN — 男装（如 S/M/L/XL/XXL/XXXL）</li>
 *   <li>CHILDREN — 童装（如 100/110/120/130/140/150）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
public enum SizeCategory {

    /** 女装 */
    WOMEN,

    /** 男装 */
    MEN,

    /** 童装 */
    CHILDREN
}
