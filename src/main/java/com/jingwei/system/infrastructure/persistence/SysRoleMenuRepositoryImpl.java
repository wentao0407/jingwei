package com.jingwei.system.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.system.domain.model.SysRoleMenu;
import com.jingwei.system.domain.repository.SysRoleMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色菜单关联仓库实现类
 * <p>
 * 基于 MyBatis-Plus Mapper 实现角色菜单关联数据持久化操作。
 * </p>
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class SysRoleMenuRepositoryImpl implements SysRoleMenuRepository {

    private final SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public int batchInsert(List<SysRoleMenu> roleMenus) {
        int count = 0;
        for (SysRoleMenu roleMenu : roleMenus) {
            count += sysRoleMenuMapper.insert(roleMenu);
        }
        return count;
    }

    @Override
    public int deleteByRoleId(Long roleId) {
        return sysRoleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)
        );
    }

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        return sysRoleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>()
                                .eq(SysRoleMenu::getRoleId, roleId)
                ).stream()
                .map(SysRoleMenu::getMenuId)
                .toList();
    }

    @Override
    public List<Long> selectMenuIdsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        // 使用 LambdaQueryWrapper 的 in 方法查询多个角色的菜单ID
        return sysRoleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>()
                                .in(SysRoleMenu::getRoleId, roleIds)
                ).stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .toList();
    }

    @Override
    public boolean existsByMenuId(Long menuId) {
        return sysRoleMenuMapper.selectCount(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getMenuId, menuId)
        ) > 0;
    }
}
