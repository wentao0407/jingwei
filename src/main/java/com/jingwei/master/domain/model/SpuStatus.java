package com.jingwei.master.domain.model;

/**
 * 款式状态枚举
 * <p>
 * SPU 的生命周期状态：
 * <ul>
 *   <li>DRAFT — 草稿，刚创建尚未启用</li>
 *   <li>ACTIVE — 启用，可在业务单据中选择</li>
 *   <li>INACTIVE — 停用，不可在业务单据中选择，已有单据引用不受影响</li>
 * </ul>
 * </p>
 * <p>
 * 与 CommonStatus 不同，SPU 多了 DRAFT 状态，因为款式创建后可能需要
 * 补充信息（如SKU价格）后才能正式启用。
 * </p>
 * <p>
 * 状态转换规则：任意状态之间均可切换，不做严格状态机约束。
 * 典型流程为 DRAFT→ACTIVE→INACTIVE，但也允许 DRAFT→INACTIVE（草稿直接废弃）
 * 或 INACTIVE→ACTIVE（重新启用）。
 * </p>
 *
 * @author JingWei
 */
public enum SpuStatus {

    /** 草稿 */
    DRAFT,

    /** 启用 */
    ACTIVE,

    /** 停用 */
    INACTIVE
}
