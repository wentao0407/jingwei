package com.jingwei.system.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据权限配置 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ConfigureDataScopeDTO {

    /** 权限维度：WAREHOUSE/DEPT/ALL */
    @NotBlank(message = "权限维度不能为空")
    private String scopeType;

    /** 权限值：ALL 或逗号分隔的ID列表 */
    @NotBlank(message = "权限值不能为空")
    private String scopeValue;
}
