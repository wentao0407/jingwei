package com.jingwei.system.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建菜单请求DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateMenuDTO {

    /** 父菜单ID，0表示顶级 */
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    /** 菜单名称 */
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    /** 菜单类型：DIRECTORY/MENU/BUTTON */
    @NotBlank(message = "菜单类型不能为空")
    private String type;

    /** 路由路径 */
    private String path;

    /** 前端组件路径 */
    private String component;

    /** 权限标识（按钮必填） */
    private String permission;

    /** 菜单图标 */
    private String icon;

    /** 排序号 */
    private Integer sortOrder;

    /** 是否可见 */
    private Boolean visible;
}
