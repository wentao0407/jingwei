package com.jingwei.master.domain.model;

import lombok.Getter;

/**
 * 流水号重置方式枚举
 * <p>
 * 控制流水号何时归零重新开始计数：
 * <ul>
 *   <li>NEVER — 从不重置，全局递增</li>
 *   <li>YEARLY — 每年重置，reset_key 为年份（如 2026）</li>
 *   <li>MONTHLY — 每月重置，reset_key 为年月（如 202604）</li>
 *   <li>DAILY — 每日重置，reset_key 为年月日（如 20260430）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum SeqResetType {

    /** 从不重置 */
    NEVER("从不重置"),
    /** 每年重置 */
    YEARLY("每年重置"),
    /** 每月重置 */
    MONTHLY("每月重置"),
    /** 每日重置 */
    DAILY("每日重置");

    private final String description;

    SeqResetType(String description) {
        this.description = description;
    }
}
