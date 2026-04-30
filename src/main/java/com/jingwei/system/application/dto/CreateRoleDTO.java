package com.jingwei.system.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建角色请求DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateRoleDTO {

    /** 角色编码 */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码最长50个字符")
    private String roleCode;

    /** 角色名称 */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 100, message = "角色名称最长100个字符")
    private String roleName;

    /** 角色描述 */
    @Size(max = 500, message = "角色描述最长500个字符")
    private String description;
}
