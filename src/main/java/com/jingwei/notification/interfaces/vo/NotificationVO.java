package com.jingwei.notification.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 通知展示 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class NotificationVO {

    /** 通知ID */
    private Long id;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 通知分类 */
    private String category;

    /** 通知分类中文标签 */
    private String categoryLabel;

    /** 关联业务类型 */
    private String businessType;

    /** 关联业务ID */
    private Long businessId;

    /** 关联业务编号 */
    private String businessNo;

    /** 发送人ID */
    private Long senderId;

    /** 是否已读 */
    private Boolean isRead;

    /** 阅读时间 */
    private LocalDateTime readAt;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
