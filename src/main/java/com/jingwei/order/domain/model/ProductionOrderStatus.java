package com.jingwei.order.domain.model;

import lombok.Getter;

/**
 * 生产订单状态枚举
 * <p>
 * 生产订单的每行有独立状态，主表状态取所有行的最滞后状态。
 * 状态流转比销售订单更细，因为车间管理需要跟踪每个款在哪个生产阶段。
 * </p>
 * <p>
 * 状态流转图：
 * <pre>
 * DRAFT → RELEASED → PLANNED → CUTTING → SEWING → FINISHING → COMPLETED → STOCKED
 *                    └──────────────────→ SEWING（跳过裁剪）
 * </pre>
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum ProductionOrderStatus {

    /** 草稿 — 初始状态，允许编辑 */
    DRAFT("草稿", 0),

    /** 已下达 — 待排产 */
    RELEASED("已下达", 1),

    /** 已排产 — 等待开始生产 */
    PLANNED("已排产", 2),

    /** 裁剪中 */
    CUTTING("裁剪中", 3),

    /** 缝制中 */
    SEWING("缝制中", 4),

    /** 后整中 */
    FINISHING("后整中", 5),

    /** 已完工 — 生产完成，待入库 */
    COMPLETED("已完工", 6),

    /** 已入库 — 入库确认，终态 */
    STOCKED("已入库", 7);

    /** 状态中文标签 */
    private final String label;

    /**
     * 状态优先级（用于计算最滞后状态）
     * <p>
     * 数值越小越滞后，主表状态取所有行中 order 最小的状态。
     * 例如：3行分别是 SEWING(4)、CUTTING(3)、RELEASED(1) → 主表状态为 RELEASED(1)。
     * </p>
     */
    private final int order;

    ProductionOrderStatus(String label, int order) {
        this.label = label;
        this.order = order;
    }
}
