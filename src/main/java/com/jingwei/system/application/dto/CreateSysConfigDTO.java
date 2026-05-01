package com.jingwei.system.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建系统配置 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateSysConfigDTO {

    @NotBlank(message = "配置键不能为空")
    @Size(max = 128, message = "配置键长度不能超过128个字符")
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    private String configValue;

    private String configGroup = "DEFAULT";

    private String description;

    private Boolean needRestart = false;
}
