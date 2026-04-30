package com.jingwei.system.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 用户权限信息VO
 * <p>
 * 登录后返回当前用户的菜单树和权限标识列表，供前端渲染菜单和控制按钮可见性。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UserPermissionVO {

    /** 菜单树（仅含有权限的目录和菜单，不含按钮） */
    private List<MenuVO> menuTree;

    /** 权限标识列表（所有按钮级权限，如 order:sales:create） */
    private List<String> permissions;
}
