package com.jingwei.system.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.system.domain.model.SysUser;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.domain.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 用户仓库实现类
 * <p>
 * 基于 MyBatis-Plus Mapper 实现用户数据持久化操作。
 * </p>
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class SysUserRepositoryImpl implements SysUserRepository {

    private final SysUserMapper sysUserMapper;

    @Override
    public SysUser selectById(Long id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    public SysUser selectByUsername(String username) {
        return sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );
    }

    @Override
    public IPage<SysUser> selectPage(Page<SysUser> page, String keyword, String status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword)
            );
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(SysUser::getStatus, UserStatus.valueOf(status));
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);
        return sysUserMapper.selectPage(page, wrapper);
    }

    @Override
    public int insert(SysUser user) {
        return sysUserMapper.insert(user);
    }

    @Override
    public int updateById(SysUser user) {
        return sysUserMapper.updateById(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        ) > 0;
    }
}
