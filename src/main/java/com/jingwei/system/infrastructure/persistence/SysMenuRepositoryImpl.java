package com.jingwei.system.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.system.domain.model.SysMenu;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.domain.repository.SysMenuRepository;
import com.jingwei.system.domain.repository.SysRoleMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜单仓库实现类
 * <p>
 * 基于 MyBatis-Plus Mapper 实现菜单数据持久化操作。
 * </p>
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class SysMenuRepositoryImpl implements SysMenuRepository {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuRepository sysRoleMenuRepository;

    @Override
    public SysMenu selectById(Long id) {
        return sysMenuMapper.selectById(id);
    }

    @Override
    public List<SysMenu> selectAll() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        // 按排序号升序，保证菜单树的顺序正确
        wrapper.orderByAsc(SysMenu::getSortOrder);
        return sysMenuMapper.selectList(wrapper);
    }

    @Override
    public List<SysMenu> selectByRoleIds(List<Long> roleIds) {
        // 先查出角色关联的菜单ID（去重）
        List<Long> menuIds = sysRoleMenuRepository.selectMenuIdsByRoleIds(roleIds);
        if (menuIds.isEmpty()) {
            return List.of();
        }

        // 再查出菜单详情，只查可见且启用的菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysMenu::getId, menuIds)
                .eq(SysMenu::getVisible, true)
                .eq(SysMenu::getStatus, UserStatus.ACTIVE)
                .orderByAsc(SysMenu::getSortOrder);
        return sysMenuMapper.selectList(wrapper);
    }

    @Override
    public int insert(SysMenu menu) {
        return sysMenuMapper.insert(menu);
    }

    @Override
    public int updateById(SysMenu menu) {
        return sysMenuMapper.updateById(menu);
    }

    @Override
    public boolean existsByPermission(String permission) {
        return sysMenuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getPermission, permission)
        ) > 0;
    }

    @Override
    public long countByParentId(Long parentId) {
        return sysMenuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getParentId, parentId)
        );
    }

    @Override
    public int deleteById(Long id) {
        return sysMenuMapper.deleteById(id);
    }
}
