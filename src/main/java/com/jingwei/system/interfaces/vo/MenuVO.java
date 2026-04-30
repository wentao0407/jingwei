package com.jingwei.system.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单VO（树形结构）
 * <p>
 * 包含子菜单列表，用于前端渲染菜单树。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class MenuVO {

    /** 菜单ID */
    private Long id;

    /** 父菜单ID */
    private Long parentId;

    /** 菜单名称 */
    private String name;

    /** 菜单类型：DIRECTORY/MENU/BUTTON */
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

    /** 状态 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 子菜单列表 */
    private List<MenuVO> children;
}
