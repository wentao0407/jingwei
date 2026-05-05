package com.jingwei.notification.infrastructure.notifier;

/**
 * 外部渠道通知推送接口
 * <p>
 * 定义外部渠道（企微/钉钉）的推送能力。
 * 每个渠道实现此接口，由 NotificationDomainService 根据渠道类型选择对应的实现。
 * </p>
 * <p>
 * 推送失败抛出异常，由调用方捕获并记录失败原因，不回滚业务事务。
 * </p>
 *
 * @author JingWei
 */
public interface ExternalNotifier {

    /**
     * 判断是否支持指定渠道
     *
     * @param channel 渠道编码（WECHAT_WORK/DINGTALK）
     * @return 是否支持
     */
    boolean supports(String channel);

    /**
     * 发送通知到外部渠道
     *
     * @param title      消息标题
     * @param content    消息内容
     * @param receiverId 接收人ID（用于查找用户手机号等推送标识）
     * @throws Exception 推送失败时抛出异常
     */
    void send(String title, String content, Long receiverId) throws Exception;
}
