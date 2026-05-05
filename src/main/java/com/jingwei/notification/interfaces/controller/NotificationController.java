package com.jingwei.notification.interfaces.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.notification.application.dto.MarkReadDTO;
import com.jingwei.notification.application.dto.NotificationQueryDTO;
import com.jingwei.notification.application.dto.UpdatePreferenceDTO;
import com.jingwei.notification.application.service.NotificationApplicationService;
import com.jingwei.notification.interfaces.vo.NotificationPreferenceVO;
import com.jingwei.notification.interfaces.vo.NotificationVO;
import com.jingwei.notification.interfaces.vo.UnreadCountVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通知中心 Controller
 * <p>
 * 提供通知查询、已读标记、偏好配置等接口。
 * 通知发送由 NotificationDomainService 在业务事件触发时调用，不对外暴露接口。
 * </p>
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationApplicationService notificationApplicationService;

    /**
     * 分页查询我的通知
     */
    @PostMapping("/notification/page")
    public R<Page<NotificationVO>> pageNotifications(@Valid @RequestBody NotificationQueryDTO dto) {
        return R.ok(notificationApplicationService.pageNotifications(dto));
    }

    /**
     * 获取未读通知数量
     */
    @PostMapping("/notification/unread-count")
    public R<UnreadCountVO> unreadCount() {
        return R.ok(notificationApplicationService.getUnreadCount());
    }

    /**
     * 标记单条通知已读
     */
    @RequirePermission("notification:read")
    @PostMapping("/notification/mark-read")
    public R<Void> markRead(@Valid @RequestBody MarkReadDTO dto) {
        notificationApplicationService.markRead(dto);
        return R.ok();
    }

    /**
     * 标记所有通知已读
     */
    @RequirePermission("notification:readAll")
    @PostMapping("/notification/mark-all-read")
    public R<Void> markAllRead() {
        notificationApplicationService.markAllRead();
        return R.ok();
    }

    /**
     * 查询通知偏好列表
     */
    @PostMapping("/notification/preference/detail")
    public R<List<NotificationPreferenceVO>> listPreferences() {
        return R.ok(notificationApplicationService.listPreferences());
    }

    /**
     * 更新通知偏好
     */
    @RequirePermission("notification:pref:update")
    @PostMapping("/notification/preference/update")
    public R<Void> updatePreference(@Valid @RequestBody UpdatePreferenceDTO dto) {
        notificationApplicationService.updatePreference(dto);
        return R.ok();
    }
}
