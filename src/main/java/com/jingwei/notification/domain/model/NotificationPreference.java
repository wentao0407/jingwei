package com.jingwei.notification.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 通知偏好配置实体
 * <p>
 * 对应数据库表 t_sys_notification_preference，记录用户对各类通知的渠道偏好。
 * 用户可按通知分类独立配置是否接收站内消息、企微推送、钉钉推送。
 * </p>
 * <p>
 * 默认偏好：站内消息=开、企微=开、钉钉=关。
 * 站内消息始终发送（不受偏好控制），偏好仅影响外部渠道推送。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_sys_notification_preference")
public class NotificationPreference extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 通知分类：APPROVAL/INVENTORY_ALERT/ORDER/QUALITY/STOCKTAKING/RETURN */
    private String category;

    /** 站内通知开关（默认开，但不受此字段控制，站内消息始终发送） */
    private Boolean channelSite;

    /** 企微推送开关 */
    private Boolean channelWechat;

    /** 钉钉推送开关 */
    private Boolean channelDingtalk;
}
