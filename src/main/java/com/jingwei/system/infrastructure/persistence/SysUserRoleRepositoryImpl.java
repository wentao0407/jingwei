package com.jingwei.system.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.system.domain.model.SysUserRole;
import com.jingwei.system.domain.repository.SysUserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色关联仓库实现类
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class SysUserRoleRepositoryImpl implements SysUserRoleRepository {

    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public int batchInsert(List<SysUserRole> userRoles) {
        int count = 0;
        for (SysUserRole userRole : userRoles) {
            count += sysUserRoleMapper.insert(userRole);
        }
        return count;
    }

    @Override
    public int deleteByUserId(Long userId) {
        return sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );
    }

    @Override
    public List<Long> selectRoleIdsByUserId(Long userId) {
        return sysUserRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>()
                                .eq(SysUserRole::getUserId, userId)
                ).stream()
                .map(SysUserRole::getRoleId)
                .toList();
    }

    @Override
    public List<Long> selectUserIdsByRoleId(Long roleId) {
        return sysUserRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>()
                                .eq(SysUserRole::getRoleId, roleId)
                ).stream()
                .map(SysUserRole::getUserId)
                .toList();
    }
}
