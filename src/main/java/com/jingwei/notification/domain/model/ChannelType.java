package com.jingwei.notification.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 通知渠道类型枚举
 * <p>
 * 定义系统支持的通知推送渠道：
 * <ul>
 *   <li>SITE — 站内消息（始终发送，不受偏好控制）</li>
 *   <li>WECHAT_WORK — 企业微信 Webhook 机器人推送</li>
 *   <li>DINGTALK — 钉钉 Webhook 机器人推送</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@RequiredArgsConstructor
public enum ChannelType {

    /** 站内消息 */
    SITE("SITE", "站内消息"),

    /** 企业微信 */
    WECHAT_WORK("WECHAT_WORK", "企业微信"),

    /** 钉钉 */
    DINGTALK("DINGTALK", "钉钉");

    /** 编码 */
    private final String code;

    /** 中文标签 */
    private final String label;
}
