package com.jingwei.system.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新菜单请求DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateMenuDTO {

    /** 父菜单ID */
    private Long parentId;

    /** 菜单名称 */
    private String name;

    /** 菜单类型 */
    private String type;

    /** 路由路径 */
    private String path;

    /** 前端组件路径 */
    private String component;

    /** 权限标识 */
    private String permission;

    /** 菜单图标 */
    private String icon;

    /** 排序号 */
    private Integer sortOrder;

    /** 是否可见 */
    private Boolean visible;

    /** 状态：ACTIVE/INACTIVE */
    private String status;
}
