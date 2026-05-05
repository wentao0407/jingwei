package com.jingwei.notification.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 站内消息实体
 * <p>
 * 对应数据库表 t_sys_notification，存储系统生成的站内通知消息。
 * 通知由领域事件触发（审批、库存预警、到货等），通过 NotificationDomainService 统一创建。
 * </p>
 * <p>
 * 一条通知可有多个接收人（通过 NotificationReceiver 关联），
 * 同时可触发外部渠道推送（通过 NotificationChannel 记录推送状态）。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_sys_notification")
public class Notification extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 通知分类：APPROVAL/INVENTORY_ALERT/ORDER/QUALITY/STOCKTAKING/RETURN */
    private String category;

    /** 关联业务类型 */
    private String businessType;

    /** 关联业务ID */
    private Long businessId;

    /** 关联业务编号 */
    private String businessNo;

    /** 发送人ID（系统消息为NULL） */
    private Long senderId;
}
