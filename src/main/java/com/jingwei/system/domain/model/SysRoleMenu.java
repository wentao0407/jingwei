package com.jingwei.system.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色菜单关联实体
 * <p>
 * 对应数据库表 t_sys_role_menu，用于角色和菜单的多对多关联。
 * 为角色分配菜单权限后，该角色用户只能看到有权限的菜单和按钮。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_sys_role_menu")
public class SysRoleMenu extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 角色ID */
    private Long roleId;

    /** 菜单ID */
    private Long menuId;
}
