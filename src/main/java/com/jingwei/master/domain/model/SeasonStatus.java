package com.jingwei.master.domain.model;

/**
 * 季节状态枚举
 * <p>
 * 季节只有两种状态：ACTIVE（进行中）和 CLOSED（已关闭）。
 * 与 CommonStatus（ACTIVE/INACTIVE）语义不同：
 * <ul>
 *   <li>ACTIVE — 季节进行中，可在业务单据中选择</li>
 *   <li>CLOSED — 季节已关闭，不可在业务单据中选择，其下波段也不可选用</li>
 * </ul>
 * </p>
 * <p>
 * 不使用 INACTIVE 而用 CLOSED，是因为"关闭"是季节的自然生命周期终点，
 * 而"停用"暗示的是管理操作，语义上不够精确。
 * </p>
 *
 * @author JingWei
 */
public enum SeasonStatus {

    /** 进行中 */
    ACTIVE,

    /** 已关闭 */
    CLOSED
}
