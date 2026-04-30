package com.jingwei.system.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新角色请求DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateRoleDTO {

    /** 角色名称 */
    @Size(max = 100, message = "角色名称最长100个字符")
    private String roleName;

    /** 角色描述 */
    @Size(max = 500, message = "角色描述最长500个字符")
    private String description;

    /** 状态：ACTIVE/INACTIVE */
    private String status;
}
