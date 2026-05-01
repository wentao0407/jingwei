package com.jingwei.system.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.application.dto.UpdateUserDTO;
import com.jingwei.system.domain.model.SysUser;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.domain.repository.SysUserRepository;
import com.jingwei.system.domain.repository.SysUserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
     * 校验用户名唯一性，密码BCrypt加密后存储，记录密码更新时间。
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

        // 记录密码更新时间，用于密码过期检查
        user.setPasswordUpdatedAt(LocalDateTime.now());

        // 默认状态为 ACTIVE
        user.setStatus(UserStatus.ACTIVE);

        sysUserRepository.insert(user);
        log.info("创建用户: username={}, id={}", user.getUsername(), user.getId());
        return user;
    }

    /**
     * 更新用户信息
     * <p>
     * 先查出 existing 实体（携带正确的 version），将 DTO 变更字段合并到 existing 上，
     * 再用 existing 进行更新，确保乐观锁 version 条件正确。
     * </p>
     *
     * @param userId 用户ID
     * @param dto    更新用户请求DTO
     * @return 更新后的用户实体
     */
    public SysUser updateUser(Long userId, UpdateUserDTO dto) {
        SysUser existing = sysUserRepository.selectById(userId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 将 DTO 中非 null 字段合并到 existing，保留正确的 version 用于乐观锁
        if (dto.getRealName() != null) {
            existing.setRealName(dto.getRealName());
        }
        if (dto.getPhone() != null) {
            existing.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            existing.setEmail(dto.getEmail());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(UserStatus.valueOf(dto.getStatus()));
        }

        int rows = sysUserRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新用户: id={}", userId);
        return sysUserRepository.selectById(userId);
    }

    /**
     * 修改密码
     * <p>
     * 验证旧密码正确性，新密码不能与旧密码相同，修改后更新 passwordUpdatedAt。
     * </p>
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserRepository.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 验证旧密码是否正确
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException(ErrorCode.OLD_PASSWORD_MISMATCH);
        }

        // 新密码不能与旧密码相同
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BizException(ErrorCode.PASSWORD_SAME_AS_OLD);
        }

        // 加密新密码并更新
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordUpdatedAt(LocalDateTime.now());

        int rows = sysUserRepository.updateById(user);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("用户修改密码: userId={}", userId);
    }

    /**
     * 检查用户密码是否已过期
     * <p>
     * 根据 t_sys_config 中 password.expiry.days 配置判断。
     * 配置值为0表示密码永不过期。
     * </p>
     *
     * @param user             用户实体
     * @param passwordExpiryDays 密码过期天数（从系统配置读取）
     * @return true=密码已过期
     */
    public boolean isPasswordExpired(SysUser user, int passwordExpiryDays) {
        // 配置为0表示密码永不过期
        if (passwordExpiryDays <= 0) {
            return false;
        }

        // passwordUpdatedAt 为空时视为已过期（兼容老数据）
        if (user.getPasswordUpdatedAt() == null) {
            return true;
        }

        LocalDateTime expiryTime = user.getPasswordUpdatedAt().plusDays(passwordExpiryDays);
        return LocalDateTime.now().isAfter(expiryTime);
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
