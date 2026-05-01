package com.jingwei.system.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新系统配置 DTO
 * <p>
 * 修改时必须填写修改原因。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateSysConfigDTO {

    private String configValue;

    private String description;

    private Boolean needRestart;

    /** 修改原因（必填） */
    @NotBlank(message = "修改原因不能为空")
    private String remark;
}
