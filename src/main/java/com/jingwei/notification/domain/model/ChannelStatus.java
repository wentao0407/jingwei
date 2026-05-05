package com.jingwei.notification.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 渠道推送状态枚举
 * <p>
 * 外部渠道（企微/钉钉）推送的生命周期状态：
 * <ul>
 *   <li>PENDING — 待发送</li>
 *   <li>SENT — 已发送</li>
 *   <li>FAILED — 发送失败（记录失败原因，可重试）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum ChannelStatus {

    /** 待发送 */
    PENDING("PENDING", "待发送"),

    /** 已发送 */
    SENT("SENT", "已发送"),

    /** 发送失败 */
    FAILED("FAILED", "发送失败");

    /** 编码 */
    private final String code;

    /** 中文标签 */
    private final String label;
}
