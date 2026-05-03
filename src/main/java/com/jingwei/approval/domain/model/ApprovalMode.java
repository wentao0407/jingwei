package com.jingwei.approval.domain.model;

import lombok.Getter;

/**
 * 审批模式枚举
 * <p>
 * 定义审批的两种模式：
 * <ul>
 *   <li>SINGLE — 单人审批：指定一个角色，该角色下任一用户审批即可</li>
 *   <li>OR_SIGN — 或签：指定多个角色，任意一人审批即通过，其他人待办自动取消</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum ApprovalMode {

    /** 单人审批：指定一个角色，该角色下任一用户审批即可 */
    SINGLE("单人审批"),

    /** 或签：指定多个角色，任意一人审批即通过 */
    OR_SIGN("或签");

    /** 中文标签 */
    private final String label;

    ApprovalMode(String label) {
        this.label = label;
    }
}
