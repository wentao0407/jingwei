package com.jingwei.notification.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.UserContext;
import com.jingwei.notification.application.dto.MarkReadDTO;
import com.jingwei.notification.application.dto.NotificationQueryDTO;
import com.jingwei.notification.application.dto.UpdatePreferenceDTO;
import com.jingwei.notification.domain.model.*;
import com.jingwei.notification.domain.repository.NotificationPreferenceRepository;
import com.jingwei.notification.domain.repository.NotificationReceiverRepository;
import com.jingwei.notification.domain.repository.NotificationRepository;
import com.jingwei.notification.domain.service.NotificationDomainService;
import com.jingwei.notification.interfaces.vo.NotificationPreferenceVO;
import com.jingwei.notification.interfaces.vo.NotificationVO;
import com.jingwei.notification.interfaces.vo.UnreadCountVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知应用服务 — 编排层
 * <p>
 * 负责通知中心的查询和用户操作编排：
 * <ul>
 *   <li>分页查询我的通知（含已读状态）</li>
 *   <li>获取未读数量</li>
 *   <li>标记已读/全部已读</li>
 *   <li>查询/更新通知偏好</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationApplicationService {

    private final NotificationDomainService notificationDomainService;
    private final NotificationRepository notificationRepository;
    private final NotificationReceiverRepository receiverRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    /**
     * 通知分类枚举到中文标签的映射
     * <p>
     * 枚举编码 → 中文标签，用于 VO 层展示。
     * 例如：APPROVAL → "审批通知"，ORDER → "订单通知"
     * </p>
     */
    private static final Map<String, String> CATEGORY_LABELS = Arrays.stream(NotificationCategory.values())
            .collect(Collectors.toMap(NotificationCategory::getCode, NotificationCategory::getLabel));

    /**
     * 分页查询我的通知
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    public Page<NotificationVO> pageNotifications(NotificationQueryDTO dto) {
        Long userId = UserContext.getUserId();
        Page<Notification> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<Notification> result = notificationRepository.selectPage(
                page, userId, dto.getCategory(), dto.getIsRead());

        // 批量查询当前用户的未读接收记录，用于填充已读状态
        List<NotificationReceiver> unreadReceivers = receiverRepository.selectUnreadByReceiverId(userId);
        // 构建未读通知ID集合：在集合中 = 未读，不在集合中 = 已读
        java.util.Set<Long> unreadIds = unreadReceivers.stream()
                .map(NotificationReceiver::getNotificationId)
                .collect(Collectors.toSet());

        Page<NotificationVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(n -> toVO(n, unreadIds.contains(n.getId())))
                .toList());
        return voPage;
    }

    /**
     * 获取未读通知数量
     *
     * @return 未读数量
     */
    public UnreadCountVO getUnreadCount() {
        Long userId = UserContext.getUserId();
        return new UnreadCountVO(notificationDomainService.countUnread(userId));
    }

    /**
     * 标记单条通知已读
     *
     * @param dto 标记已读 DTO
     */
    public void markRead(MarkReadDTO dto) {
        Long userId = UserContext.getUserId();
        notificationDomainService.markRead(dto.getNotificationId(), userId);
    }

    /**
     * 标记所有通知已读
     *
     * @return 标记数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int markAllRead() {
        Long userId = UserContext.getUserId();
        return notificationDomainService.markAllRead(userId);
    }

    /**
     * 查询当前用户的通知偏好列表
     *
     * @return 偏好列表
     */
    public List<NotificationPreferenceVO> listPreferences() {
        Long userId = UserContext.getUserId();
        List<NotificationPreference> prefs = preferenceRepository.selectByUserId(userId);

        // 如果用户尚未配置偏好，返回默认值
        if (prefs.isEmpty()) {
            return Arrays.stream(NotificationCategory.values())
                    .map(cat -> {
                        NotificationPreferenceVO vo = new NotificationPreferenceVO();
                        vo.setCategory(cat.getCode());
                        vo.setCategoryLabel(cat.getLabel());
                        vo.setChannelSite(true);
                        vo.setChannelWechat(true);
                        vo.setChannelDingtalk(false);
                        return vo;
                    })
                    .toList();
        }

        return prefs.stream().map(this::toPreferenceVO).toList();
    }

    /**
     * 更新通知偏好
     *
     * @param dto 更新偏好 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePreference(UpdatePreferenceDTO dto) {
        Long userId = UserContext.getUserId();
        NotificationPreference pref = new NotificationPreference();
        pref.setUserId(userId);
        pref.setCategory(dto.getCategory());
        pref.setChannelSite(dto.getChannelSite() != null ? dto.getChannelSite() : true);
        pref.setChannelWechat(dto.getChannelWechat() != null ? dto.getChannelWechat() : true);
        pref.setChannelDingtalk(dto.getChannelDingtalk() != null ? dto.getChannelDingtalk() : false);
        preferenceRepository.insertOrUpdate(pref);
    }

    // ==================== 私有方法 ====================

    private NotificationVO toVO(Notification notification, boolean isUnread) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setCategory(notification.getCategory());
        vo.setCategoryLabel(CATEGORY_LABELS.getOrDefault(notification.getCategory(), notification.getCategory()));
        vo.setBusinessType(notification.getBusinessType());
        vo.setBusinessId(notification.getBusinessId());
        vo.setBusinessNo(notification.getBusinessNo());
        vo.setSenderId(notification.getSenderId());
        vo.setIsRead(!isUnread);
        vo.setCreatedAt(notification.getCreatedAt());
        return vo;
    }

    private NotificationPreferenceVO toPreferenceVO(NotificationPreference pref) {
        NotificationPreferenceVO vo = new NotificationPreferenceVO();
        vo.setId(pref.getId());
        vo.setCategory(pref.getCategory());
        vo.setCategoryLabel(CATEGORY_LABELS.getOrDefault(pref.getCategory(), pref.getCategory()));
        vo.setChannelSite(pref.getChannelSite());
        vo.setChannelWechat(pref.getChannelWechat());
        vo.setChannelDingtalk(pref.getChannelDingtalk());
        return vo;
    }
}
