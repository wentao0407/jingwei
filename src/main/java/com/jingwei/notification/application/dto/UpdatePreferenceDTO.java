package com.jingwei.notification.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新通知偏好 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdatePreferenceDTO {

    /** 通知分类 */
    @NotBlank(message = "通知分类不能为空")
    private String category;

    /** 站内通知开关 */
    private Boolean channelSite;

    /** 企微推送开关 */
    private Boolean channelWechat;

    /** 钉钉推送开关 */
    private Boolean channelDingtalk;
}
