package com.jingwei.system.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 登录响应VO
 * <p>
 * 登录成功后返回用户信息、Token、权限标识列表和授权菜单树。
 * 菜单树仅包含目录和菜单类型节点（不含按钮），前端据此渲染侧边栏导航。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class LoginVO {

    /** JWT Token */
    private String token;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 角色ID列表 */
    private List<Long> roleIds;

    /** 权限标识列表（按钮级权限） */
    private List<String> permissions;

    /** 授权菜单树（仅目录+菜单，不含按钮） */
    private List<MenuVO> menuTree;
}
