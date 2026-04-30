package com.jingwei.system.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.system.domain.model.SysRole;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.domain.repository.SysRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 角色仓库实现类
 * <p>
 * 基于 MyBatis-Plus Mapper 实现角色数据持久化操作。
 * </p>
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class SysRoleRepositoryImpl implements SysRoleRepository {

    private final SysRoleMapper sysRoleMapper;

    @Override
    public SysRole selectById(Long id) {
        return sysRoleMapper.selectById(id);
    }

    @Override
    public IPage<SysRole> selectPage(Page<SysRole> page, String keyword, String status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(SysRole::getRoleCode, keyword)
                    .or().like(SysRole::getRoleName, keyword)
            );
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(SysRole::getStatus, UserStatus.valueOf(status));
        }
        wrapper.orderByDesc(SysRole::getCreatedAt);
        return sysRoleMapper.selectPage(page, wrapper);
    }

    @Override
    public int insert(SysRole role) {
        return sysRoleMapper.insert(role);
    }

    @Override
    public int updateById(SysRole role) {
        return sysRoleMapper.updateById(role);
    }

    @Override
    public boolean existsByRoleCode(String roleCode) {
        return sysRoleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, roleCode)
        ) > 0;
    }
}
