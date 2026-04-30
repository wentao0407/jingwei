package com.jingwei.system.application.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 分配角色请求DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class AssignRoleDTO {

    /** 角色ID列表 */
    @NotEmpty(message = "角色列表不能为空")
    private List<Long> roleIds;
}
