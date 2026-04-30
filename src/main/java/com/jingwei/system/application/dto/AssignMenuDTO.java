package com.jingwei.system.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 角色分配菜单权限请求DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class AssignMenuDTO {

    /** 角色ID */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /** 菜单ID列表（包含目录、菜单和按钮的ID） */
    @NotEmpty(message = "菜单ID列表不能为空")
    private List<Long> menuIds;
}
