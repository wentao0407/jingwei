package com.jingwei.system.domain.repository;

import com.jingwei.system.domain.model.SysRoleMenu;

import java.util.List;

/**
 * 角色菜单关联仓库接口
 * <p>
 * 定义角色菜单关联数据的持久化操作，由 infrastructure 层实现。
 * </p>
 *
 * @author JingWei
 */
public interface SysRoleMenuRepository {

    /**
     * 批量新增角色菜单关联
     *
     * @param roleMenus 角色菜单关联列表
     * @return 影响行数
     */
    int batchInsert(List<SysRoleMenu> roleMenus);

    /**
     * 删除角色的所有菜单关联
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteByRoleId(Long roleId);

    /**
     * 查询角色拥有的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);

    /**
     * 根据角色ID列表查询所有菜单ID（去重）
     *
     * @param roleIds 角色ID列表
     * @return 菜单ID列表（去重）
     */
    List<Long> selectMenuIdsByRoleIds(List<Long> roleIds);

    /**
     * 检查菜单是否被角色引用
     *
     * @param menuId 菜单ID
     * @return true=被引用
     */
    boolean existsByMenuId(Long menuId);
}
