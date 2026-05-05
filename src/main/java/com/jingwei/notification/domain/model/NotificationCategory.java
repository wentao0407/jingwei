package com.jingwei.notification.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 通知分类枚举
 * <p>
 * 定义系统中各类业务通知的分类，用于：
 * <ul>
 *   <li>通知偏好配置 — 用户可按分类配置推送渠道</li>
 *   <li>通知列表筛选 — 用户按分类筛选通知</li>
 *   <li>事件订阅匹配 — 不同领域事件映射到不同分类</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum NotificationCategory {

    /** 审批相关（待审批、审批通过、审批驳回） */
    APPROVAL("APPROVAL", "审批"),

    /** 库存预警（低库存、超储、库龄超期） */
    INVENTORY_ALERT("INVENTORY_ALERT", "库存预警"),

    /** 订单相关（订单状态变更） */
    ORDER("ORDER", "订单"),

    /** 质检相关（到货待检） */
    QUALITY("QUALITY", "质检"),

    /** 盘点相关（盘点差异需审核） */
    STOCKTAKING("STOCKTAKING", "盘点"),

    /** 退货相关 */
    RETURN("RETURN", "退货");

    /** 编码 */
    private final String code;

    /** 中文标签 */
    private final String label;
}
