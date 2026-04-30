package com.jingwei.system.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.domain.model.SysUser;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.domain.repository.SysUserRepository;
import com.jingwei.system.domain.repository.SysUserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户领域服务
 * <p>
 * 封装用户相关的纯业务逻辑，不涉及事务编排。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final SysUserRepository sysUserRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建用户
     * <p>
     * 校验用户名唯一性，密码BCrypt加密后存储。
     * </p>
     *
     * @param user     用户实体（password 字段为明文）
     * @return 保存后的用户实体（password 已加密）
     */
    public SysUser createUser(SysUser user) {
        // 校验用户名唯一性
        if (sysUserRepository.existsByUsername(user.getUsername())) {
            throw new BizException(ErrorCode.USERNAME_DUPLICATE);
        }

        // 密码BCrypt加密，数据库中不存明文
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 默认状态为 ACTIVE
        user.setStatus(UserStatus.ACTIVE);

        sysUserRepository.insert(user);
        log.info("创建用户: username={}, id={}", user.getUsername(), user.getId());
        return user;
    }

    /**
     * 更新用户信息
     *
     * @param user 用户实体
     * @return 更新后的用户实体
     */
    public SysUser updateUser(SysUser user) {
        SysUser existing = sysUserRepository.selectById(user.getId());
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "用户不存在");
        }

        int rows = sysUserRepository.updateById(user);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新用户: id={}", user.getId());
        return sysUserRepository.selectById(user.getId());
    }

    /**
     * 停用用户
     * <p>
     * 停用后用户不能登录。
     * </p>
     *
     * @param userId 用户ID
     */
    public void deactivateUser(Long userId) {
        SysUser user = sysUserRepository.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "用户不存在");
        }
        user.setStatus(UserStatus.INACTIVE);
        sysUserRepository.updateById(user);
        log.info("停用用户: id={}", userId);
    }

    /**
     * 为用户分配角色
     * <p>
     * 先删除原有角色关联，再批量插入新关联。
     * </p>
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    public void assignRoles(Long userId, java.util.List<Long> roleIds) {
        SysUser user = sysUserRepository.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 先删除旧关联
        sysUserRoleRepository.deleteByUserId(userId);

        // 批量插入新关联
        if (!roleIds.isEmpty()) {
            java.util.List<com.jingwei.system.domain.model.SysUserRole> userRoles = roleIds.stream()
                    .map(roleId -> {
                        com.jingwei.system.domain.model.SysUserRole ur = new com.jingwei.system.domain.model.SysUserRole();
                        ur.setUserId(userId);
                        ur.setRoleId(roleId);
                        return ur;
                    })
                    .toList();
            sysUserRoleRepository.batchInsert(userRoles);
        }

        log.info("分配角色: userId={}, roleIds={}", userId, roleIds);
    }

    /**
     * 获取用户角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    public java.util.List<Long> getUserRoleIds(Long userId) {
        return sysUserRoleRepository.selectRoleIdsByUserId(userId);
    }

    /**
     * 分页查询用户
     *
     * @param page    分页参数
     * @param keyword 搜索关键词
     * @param status  状态筛选
     * @return 分页结果
     */
    public IPage<SysUser> pageQuery(Page<SysUser> page, String keyword, String status) {
        return sysUserRepository.selectPage(page, keyword, status);
    }

    /**
     * 获取用户
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    public SysUser getUser(Long userId) {
        SysUser user = sysUserRepository.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "用户不存在");
        }
        return user;
    }
}
