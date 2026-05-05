package com.jingwei.notification.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 标记已读 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class MarkReadDTO {

    /** 通知ID */
    @NotNull(message = "通知ID不能为空")
    private Long notificationId;
}
