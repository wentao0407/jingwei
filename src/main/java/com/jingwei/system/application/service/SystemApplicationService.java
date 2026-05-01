package com.jingwei.system.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.system.application.dto.*;
import com.jingwei.system.domain.model.SysRole;
import com.jingwei.system.domain.model.SysUser;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.domain.service.RoleDomainService;
import com.jingwei.system.domain.service.UserDomainService;
import com.jingwei.system.interfaces.vo.RoleVO;
import com.jingwei.system.interfaces.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统管理应用服务
 * <p>
 * 负责用户和角色的业务编排，持有事务边界。
 * Controller 只调用此服务，不直接调用 DomainService。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemApplicationService {

    private final UserDomainService userDomainService;
    private final RoleDomainService roleDomainService;

    // ==================== 用户管理 ====================

    /**
     * 创建用户
     *
     * @param dto 创建用户请求
     * @return 用户VO
     */
    @Transactional(rollbackFor = Exception.class)
    public UserVO createUser(CreateUserDTO dto) {
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRealName(dto.getRealName() != null ? dto.getRealName() : "");
        user.setPhone(dto.getPhone() != null ? dto.getPhone() : "");
        user.setEmail(dto.getEmail() != null ? dto.getEmail() : "");

        userDomainService.createUser(user);
        return toUserVO(user, List.of());
    }

    /**
     * 更新用户
     *
     * @param userId 用户ID
     * @param dto    更新用户请求
     * @return 用户VO
     */
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(Long userId, UpdateUserDTO dto) {
        SysUser updated = userDomainService.updateUser(userId, dto);
        List<Long> roleIds = userDomainService.getUserRoleIds(userId);
        return toUserVO(updated, roleIds);
    }

    /**
     * 停用用户
     *
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deactivateUser(Long userId) {
        userDomainService.deactivateUser(userId);
    }

    /**
     * 分页查询用户
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    public IPage<UserVO> pageUser(UserQueryDTO dto) {
        Page<SysUser> page = new Page<>(dto.getCurrent(), dto.getSize());
        IPage<SysUser> userPage = userDomainService.pageQuery(page, dto.getKeyword(), dto.getStatus());
        return userPage.convert(user -> {
            List<Long> roleIds = userDomainService.getUserRoleIds(user.getId());
            return toUserVO(user, roleIds);
        });
    }

    /**
     * 查询用户详情
     *
     * @param userId 用户ID
     * @return 用户VO
     */
    public UserVO getUser(Long userId) {
        SysUser user = userDomainService.getUser(userId);
        List<Long> roleIds = userDomainService.getUserRoleIds(userId);
        return toUserVO(user, roleIds);
    }

    /**
     * 分配角色
     *
     * @param userId 用户ID
     * @param dto    分配角色请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, AssignRoleDTO dto) {
        userDomainService.assignRoles(userId, dto.getRoleIds());
    }

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param dto    修改密码请求（旧密码+新密码）
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        userDomainService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
    }

    // ==================== 角色管理 ====================

    /**
     * 创建角色
     *
     * @param dto 创建角色请求
     * @return 角色VO
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleVO createRole(CreateRoleDTO dto) {
        SysRole role = new SysRole();
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription() != null ? dto.getDescription() : "");

        roleDomainService.createRole(role);
        return toRoleVO(role);
    }

    /**
     * 更新角色
     *
     * @param roleId 角色ID
     * @param dto    更新角色请求
     * @return 角色VO
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleVO updateRole(Long roleId, UpdateRoleDTO dto) {
        SysRole updated = roleDomainService.updateRole(roleId, dto);
        return toRoleVO(updated);
    }

    /**
     * 分页查询角色
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    public IPage<RoleVO> pageRole(RoleQueryDTO dto) {
        Page<SysRole> page = new Page<>(dto.getCurrent(), dto.getSize());
        IPage<SysRole> rolePage = roleDomainService.pageQuery(page, dto.getKeyword(), dto.getStatus());
        return rolePage.convert(this::toRoleVO);
    }

    // ==================== 转换方法 ====================

    private UserVO toUserVO(SysUser user, List<Long> roleIds) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus().name());
        vo.setRoleIds(roleIds);
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }

    private RoleVO toRoleVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDescription(role.getDescription());
        vo.setStatus(role.getStatus().name());
        vo.setCreatedAt(role.getCreatedAt());
        vo.setUpdatedAt(role.getUpdatedAt());
        return vo;
    }
}
