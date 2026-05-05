package com.jingwei.notification.domain.service;

import com.jingwei.common.domain.model.DomainEvent;
import com.jingwei.notification.domain.model.NotificationCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 领域事件 → 通知 监听器
 * <p>
 * 监听 Outbox 投递出来的领域事件，自动生成通知消息。
 * 当前覆盖的事件类型：
 * <ul>
 *   <li>ApprovalPassed — 审批通过通知提交人</li>
 *   <li>ApprovalAutoPassed — 审批自动通过通知提交人</li>
 *   <li>ApprovalRejected — 审批驳回通知提交人</li>
 * </ul>
 * </p>
 * <p>
 * 使用 @Async 异步执行，避免阻塞业务主流程。
 * 通知发送失败不影响业务事务（通知服务内部已做容错）。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventNotificationListener {

    private final NotificationDomainService notificationService;

    /**
     * 审批通过事件 → 发送通知
     */
    @EventListener(condition = "#event.eventType == 'ApprovalPassed'")
    public void onApprovalPassed(DomainEvent event) {
        Map<String, Object> payload = event.getPayload();
        String businessNo = (String) payload.getOrDefault("businessNo", "");
        String businessType = (String) payload.getOrDefault("businessType", "");
        Long submitterId = getSubmitterId(event);

        if (submitterId == null) {
            log.debug("审批通过事件无法获取提交人ID，跳过通知: eventType={}, aggregateId={}",
                    event.getEventType(), event.getAggregateId());
            return;
        }

        notificationService.sendNotification(
                NotificationCategory.APPROVAL,
                "审批通过",
                "您的单据[" + businessNo + "]已审批通过",
                businessType, event.getAggregateId(), businessNo,
                null, List.of(submitterId));

        log.debug("审批通过通知已发送: businessNo={}, submitterId={}", businessNo, submitterId);
    }

    /**
     * 审批自动通过事件 → 发送通知
     */
    @EventListener(condition = "#event.eventType == 'ApprovalAutoPassed'")
    public void onApprovalAutoPassed(DomainEvent event) {
        Map<String, Object> payload = event.getPayload();
        String businessNo = (String) payload.getOrDefault("businessNo", "");
        String businessType = (String) payload.getOrDefault("businessType", "");
        Long submitterId = getSubmitterId(event);

        if (submitterId == null) {
            log.debug("审批自动通过事件无法获取提交人ID，跳过通知: aggregateId={}", event.getAggregateId());
            return;
        }

        notificationService.sendNotification(
                NotificationCategory.APPROVAL,
                "审批自动通过",
                "您的单据[" + businessNo + "]已自动审批通过",
                businessType, event.getAggregateId(), businessNo,
                null, List.of(submitterId));

        log.debug("审批自动通过通知已发送: businessNo={}", businessNo);
    }

    /**
     * 审批驳回事件 → 发送通知
     */
    @EventListener(condition = "#event.eventType == 'ApprovalRejected'")
    public void onApprovalRejected(DomainEvent event) {
        Map<String, Object> payload = event.getPayload();
        String businessNo = (String) payload.getOrDefault("businessNo", "");
        String businessType = (String) payload.getOrDefault("businessType", "");
        String opinion = (String) payload.getOrDefault("opinion", "");
        Long submitterId = getSubmitterId(event);

        if (submitterId == null) {
            log.debug("审批驳回事件无法获取提交人ID，跳过通知: aggregateId={}", event.getAggregateId());
            return;
        }

        notificationService.sendNotification(
                NotificationCategory.APPROVAL,
                "审批驳回",
                "您的单据[" + businessNo + "]已被驳回，原因：" + opinion,
                businessType, event.getAggregateId(), businessNo,
                null, List.of(submitterId));

        log.debug("审批驳回通知已发送: businessNo={}, opinion={}", businessNo, opinion);
    }

    /**
     * 从事件 payload 或上下文获取提交人ID
     * <p>
     * 优先从 payload 的 submitterId 获取，
     * 如果没有则从 createdBy 获取（部分事件可能携带）。
     * </p>
     */
    private Long getSubmitterId(DomainEvent event) {
        Map<String, Object> payload = event.getPayload();
        if (payload == null) return null;

        // 优先取 submitterId
        Object submitterId = payload.get("submitterId");
        if (submitterId instanceof Number) {
            return ((Number) submitterId).longValue();
        }

        // 其次取 createdBy
        Object createdBy = payload.get("createdBy");
        if (createdBy instanceof Number) {
            return ((Number) createdBy).longValue();
        }

        return null;
    }
}
