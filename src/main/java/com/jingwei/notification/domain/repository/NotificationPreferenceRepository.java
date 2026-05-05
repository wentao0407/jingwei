package com.jingwei.notification.domain.repository;

import com.jingwei.notification.domain.model.NotificationPreference;

import java.util.List;

/**
 * 通知偏好仓储接口
 *
 * @author JingWei
 */
public interface NotificationPreferenceRepository {

    /**
     * 插入或更新通知偏好
     *
     * @param preference 偏好实体
     * @return 影响行数
     */
    int insertOrUpdate(NotificationPreference preference);

    /**
     * 查询用户所有通知偏好
     *
     * @param userId 用户ID
     * @return 偏好列表
     */
    List<NotificationPreference> selectByUserId(Long userId);

    /**
     * 查询用户指定分类的通知偏好
     *
     * @param userId   用户ID
     * @param category 通知分类
     * @return 偏好实体，不存在返回null
     */
    NotificationPreference selectByUserIdAndCategory(Long userId, String category);

    /**
     * 批量插入或更新通知偏好
     *
     * @param preferences 偏好列表
     * @return 影响行数
     */
    int insertOrUpdateBatch(List<NotificationPreference> preferences);
}
