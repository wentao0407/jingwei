package com.jingwei.notification.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.notification.domain.model.NotificationPreference;
import com.jingwei.notification.domain.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 通知偏好仓储实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class NotificationPreferenceRepositoryImpl implements NotificationPreferenceRepository {

    private final NotificationPreferenceMapper preferenceMapper;

    @Override
    public int insertOrUpdate(NotificationPreference preference) {
        NotificationPreference existing = selectByUserIdAndCategory(
                preference.getUserId(), preference.getCategory());
        if (existing != null) {
            existing.setChannelSite(preference.getChannelSite());
            existing.setChannelWechat(preference.getChannelWechat());
            existing.setChannelDingtalk(preference.getChannelDingtalk());
            return preferenceMapper.updateById(existing);
        }
        return preferenceMapper.insert(preference);
    }

    @Override
    public List<NotificationPreference> selectByUserId(Long userId) {
        return preferenceMapper.selectList(
                new LambdaQueryWrapper<NotificationPreference>()
                        .eq(NotificationPreference::getUserId, userId));
    }

    @Override
    public NotificationPreference selectByUserIdAndCategory(Long userId, String category) {
        return preferenceMapper.selectOne(
                new LambdaQueryWrapper<NotificationPreference>()
                        .eq(NotificationPreference::getUserId, userId)
                        .eq(NotificationPreference::getCategory, category));
    }

    @Override
    public int insertOrUpdateBatch(List<NotificationPreference> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (NotificationPreference pref : preferences) {
            count += insertOrUpdate(pref);
        }
        return count;
    }
}
