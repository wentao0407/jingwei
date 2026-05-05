package com.jingwei.notification.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 外部渠道推送记录实体
 * <p>
 * 对应数据库表 t_sys_notification_channel，记录每条通知在外部渠道（企微/钉钉）的推送状态。
 * 推送失败不影响业务事务，仅记录失败原因和重试次数。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_sys_notification_channel")
public class NotificationChannel extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 通知ID */
    private Long notificationId;

    /** 接收人ID */
    private Long receiverId;

    /** 渠道类型：WECHAT_WORK/DINGTALK */
    private String channel;

    /** 推送状态：PENDING/SENT/FAILED */
    private String status;

    /** 发送时间 */
    private LocalDateTime sentAt;

    /** 失败原因 */
    private String errorMessage;

    /** 重试次数 */
    private Integer retryCount;
}
