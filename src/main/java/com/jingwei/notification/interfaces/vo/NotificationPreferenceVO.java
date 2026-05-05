package com.jingwei.notification.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 通知偏好展示 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class NotificationPreferenceVO {

    /** 偏好ID */
    private Long id;

    /** 通知分类 */
    private String category;

    /** 通知分类中文标签 */
    private String categoryLabel;

    /** 站内通知开关 */
    private Boolean channelSite;

    /** 企微推送开关 */
    private Boolean channelWechat;

    /** 钉钉推送开关 */
    private Boolean channelDingtalk;
}
