package com.jingwei.system.domain.repository;

import com.jingwei.system.domain.model.SysMenu;

import java.util.List;

/**
 * 菜单仓库接口
 * <p>
 * 定义菜单数据的持久化操作，由 infrastructure 层实现。
 * </p>
 *
 * @author JingWei
 */
public interface SysMenuRepository {

    /**
     * 根据ID查询菜单
     *
     * @param id 菜单ID
     * @return 菜单实体，不存在返回 null
     */
    SysMenu selectById(Long id);

    /**
     * 查询所有菜单（用于构建菜单树）
     *
     * @return 所有未删除的菜单列表
     */
    List<SysMenu> selectAll();

    /**
     * 根据角色ID列表查询菜单（用于获取用户有权限的菜单）
     *
     * @param roleIds 角色ID列表
     * @return 菜单列表
     */
    List<SysMenu> selectByRoleIds(List<Long> roleIds);

    /**
     * 新增菜单
     *
     * @param menu 菜单实体
     * @return 影响行数
     */
    int insert(SysMenu menu);

    /**
     * 更新菜单
     *
     * @param menu 菜单实体
     * @return 影响行数
     */
    int updateById(SysMenu menu);

    /**
     * 检查权限标识是否已存在
     *
     * @param permission 权限标识
     * @return true=已存在
     */
    boolean existsByPermission(String permission);

    /**
     * 查询指定父菜单下的子菜单数量
     *
     * @param parentId 父菜单ID
     * @return 子菜单数量
     */
    long countByParentId(Long parentId);

    /**
     * 根据ID删除菜单（逻辑删除）
     *
     * @param id 菜单ID
     * @return 影响行数
     */
    int deleteById(Long id);
}
