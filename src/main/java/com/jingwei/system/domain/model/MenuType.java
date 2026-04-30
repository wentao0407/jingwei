package com.jingwei.system.domain.model;

import lombok.Getter;

/**
 * 菜单类型枚举
 * <p>
 * 三级菜单结构：
 * <ul>
 *   <li>DIRECTORY — 目录，仅作为路由分组容器，对应前端侧边栏一级菜单</li>
 *   <li>MENU — 菜单，对应一个可访问的页面，有路由路径和前端组件</li>
 *   <li>BUTTON — 按钮，页面内的操作权限标识，如"创建"、"审批"等</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
public enum MenuType {

    /** 目录 */
    DIRECTORY("目录"),
    /** 菜单 */
    MENU("菜单"),
    /** 按钮 */
    BUTTON("按钮");

    private final String description;

    MenuType(String description) {
        this.description = description;
    }
}
