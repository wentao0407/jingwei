package com.jingwei.system.application.service;

import com.jingwei.common.config.JwtUtil;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.application.dto.LoginDTO;
import com.jingwei.system.domain.model.SysUser;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.domain.repository.SysUserRepository;
import com.jingwei.system.domain.repository.SysUserRoleRepository;
import com.jingwei.system.interfaces.vo.LoginVO;
import com.jingwei.system.interfaces.vo.UserPermissionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证应用服务
 * <p>
 * 处理用户登录，验证密码后生成 JWT Token，并返回用户权限信息。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final SysUserRepository sysUserRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final MenuApplicationService menuApplicationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录
     * <p>
     * 验证流程：
     * 1. 根据用户名查询用户
     * 2. 校验用户状态（停用用户不能登录）
     * 3. 使用 BCryptPasswordEncoder.matches() 校验密码
     * 4. 生成 JWT Token 返回
     * 5. 加载用户菜单树和权限标识列表
     * </p>
     *
     * @param dto 登录请求
     * @return 登录响应（Token + 用户信息 + 菜单树 + 权限标识）
     */
    public LoginVO login(LoginDTO dto) {
        // 根据用户名查询用户
        SysUser user = sysUserRepository.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }

        // 校验用户状态：停用用户不能登录
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BizException(ErrorCode.USER_INACTIVE);
        }

        // 使用 BCryptPasswordEncoder.matches() 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }

        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 查询用户角色
        List<Long> roleIds = sysUserRoleRepository.selectRoleIdsByUserId(user.getId());

        // 一次性加载用户菜单树和权限标识列表
        UserPermissionVO userPerm = menuApplicationService.getUserPermissionsByUserId(user.getId());

        log.info("用户登录成功: username={}, userId={}, permissions={}", user.getUsername(), user.getId(), userPerm.getPermissions().size());

        // 构造返回
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRoleIds(roleIds);
        vo.setPermissions(userPerm.getPermissions());
        vo.setMenuTree(userPerm.getMenuTree());
        return vo;
    }
}
