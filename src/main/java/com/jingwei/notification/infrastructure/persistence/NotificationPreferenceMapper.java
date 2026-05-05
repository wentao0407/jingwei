package com.jingwei.notification.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.notification.domain.model.NotificationPreference;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知偏好 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface NotificationPreferenceMapper extends BaseMapper<NotificationPreference> {
}
