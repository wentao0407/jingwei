package com.jingwei.notification.infrastructure.notifier;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 钉钉 Webhook 机器人推送
 * <p>
 * 通过钉钉群机器人 Webhook URL 推送 Markdown 格式消息。
 * 需要在 application.yml 中配置 notification.dingtalk.webhook-url。
 * </p>
 * <p>
 * 如果 webhook-url 未配置或为空，则跳过推送（不报错）。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
public class DingtalkNotifier implements ExternalNotifier {

    @Value("${notification.dingtalk.webhook-url:}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean supports(String channel) {
        return "DINGTALK".equals(channel);
    }

    @Override
    public void send(String title, String content, Long receiverId) throws Exception {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.debug("钉钉 Webhook URL 未配置，跳过推送");
            return;
        }

        Map<String, Object> message = Map.of(
                "msgtype", "markdown",
                "markdown", Map.of(
                        "title", title,
                        "text", "## " + title + "\n\n" + content
                )
        );

        try {
            restTemplate.postForEntity(webhookUrl, message, String.class);
            log.debug("钉钉推送成功: title={}", title);
        } catch (Exception e) {
            log.error("钉钉推送失败: title={}", title, e);
            throw e;
        }
    }
}
