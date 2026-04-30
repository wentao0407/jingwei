package com.jingwei.system.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 菜单实体
 * <p>
 * 对应数据库表 t_sys_menu，支持三级树形结构：目录→菜单→按钮。
 * 继承 BaseEntity 获得审计字段和乐观锁。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_sys_menu")
public class SysMenu extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 父菜单ID，0表示顶级 */
    private Long parentId;

    /** 菜单名称 */
    private String name;

    /** 菜单类型：DIRECTORY/MENU/BUTTON */
    private MenuType type;

    /** 路由路径（目录和菜单使用） */
    private String path;

    /** 前端组件路径（菜单使用） */
    private String component;

    /** 权限标识（按钮使用，如 order:sales:create） */
    private String permission;

    /** 菜单图标 */
    private String icon;

    /** 排序号，越小越靠前 */
    private Integer sortOrder;

    /** 是否可见 */
    private Boolean visible;

    /** 状态：ACTIVE/INACTIVE */
    private UserStatus status;
}
