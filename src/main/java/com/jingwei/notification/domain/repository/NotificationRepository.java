package com.jingwei.notification.domain.repository;

import com.jingwei.notification.domain.model.Notification;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 站内消息仓储接口
 *
 * @author JingWei
 */
public interface NotificationRepository {

    /**
     * 插入通知
     *
     * @param notification 通知实体
     * @return 影响行数
     */
    int insert(Notification notification);

    /**
     * 根据ID查询通知
     *
     * @param id 通知ID
     * @return 通知实体，不存在返回null
     */
    Notification selectById(Long id);

    /**
     * 分页查询通知（按接收人）
     *
     * @param page       分页参数
     * @param receiverId 接收人ID
     * @param category   通知分类（可选）
     * @param isRead     是否已读（可选）
     * @return 分页结果（通知实体列表）
     */
    Page<Notification> selectPage(Page<Notification> page, Long receiverId,
                                  String category, Boolean isRead);

    /**
     * 查询未读通知数量
     *
     * @param receiverId 接收人ID
     * @return 未读数量
     */
    long countUnread(Long receiverId);
}
