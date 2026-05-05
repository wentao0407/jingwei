package com.jingwei.notification.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.notification.domain.model.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内消息 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
