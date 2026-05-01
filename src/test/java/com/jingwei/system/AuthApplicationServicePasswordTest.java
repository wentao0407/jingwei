package com.jingwei.system;

import com.jingwei.common.config.JwtUtil;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.application.dto.LoginDTO;
import com.jingwei.system.application.service.AuthApplicationService;
import com.jingwei.system.application.service.MenuApplicationService;
import com.jingwei.system.domain.model.SysConfig;
import com.jingwei.system.domain.model.SysUser;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.domain.repository.SysUserRepository;
import com.jingwei.system.domain.repository.SysUserRoleRepository;
import com.jingwei.system.domain.service.SysConfigDomainService;
import com.jingwei.system.domain.service.UserDomainService;
import com.jingwei.system.interfaces.vo.LoginVO;
import com.jingwei.system.interfaces.vo.UserPermissionVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证应用服务单元测试 — 密码过期检查
 * <p>
 * 覆盖：登录时密码过期标记返回、密码不过期场景、配置读取容错。
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class AuthApplicationServicePasswordTest {

    @Mock
    private SysUserRepository sysUserRepository;

    @Mock
    private SysUserRoleRepository sysUserRoleRepository;

    @Mock
    private MenuApplicationService menuApplicationService;

    @Mock
    private UserDomainService userDomainService;

    @Mock
    private SysConfigDomainService sysConfigDomainService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthApplicationService authApplicationService;

    @Nested
    @DisplayName("登录 — 密码过期检查")
    class LoginPasswordExpiryTests {

        private SysUser buildActiveUser() {
            SysUser user = new SysUser();
            user.setId(1L);
            user.setUsername("testuser");
            user.setPassword("$2a$10$hashed");
            user.setRealName("测试用户");
            user.setStatus(UserStatus.ACTIVE);
            return user;
        }

        @Test
        @DisplayName("登录时密码未过期 — passwordExpired 应为 false")
        void login_passwordNotExpired_shouldReturnFalse() {
            SysUser user = buildActiveUser();
            user.setPasswordUpdatedAt(LocalDateTime.now().minusDays(10));

            when(sysUserRepository.selectByUsername("testuser")).thenReturn(user);
            when(passwordEncoder.matches("Test1234", "$2a$10$hashed")).thenReturn(true);
            when(sysConfigDomainService.getByConfigKey("password.expiry.days"))
                    .thenReturn(buildPasswordConfig("90"));
            when(userDomainService.isPasswordExpired(user, 90)).thenReturn(false);
            when(jwtUtil.generateToken(1L, "testuser")).thenReturn("jwt-token");
            when(sysUserRoleRepository.selectRoleIdsByUserId(1L)).thenReturn(List.of(1L));
            when(menuApplicationService.getUserPermissionsByUserId(1L)).thenReturn(buildUserPermission());

            LoginVO result = authApplicationService.login(buildLoginDTO("Test1234"));

            assertFalse(result.isPasswordExpired(), "密码未过期时 passwordExpired 应为 false");
        }

        @Test
        @DisplayName("登录时密码已过期 — passwordExpired 应为 true")
        void login_passwordExpired_shouldReturnTrue() {
            SysUser user = buildActiveUser();
            user.setPasswordUpdatedAt(LocalDateTime.now().minusDays(100));

            when(sysUserRepository.selectByUsername("testuser")).thenReturn(user);
            when(passwordEncoder.matches("Test1234", "$2a$10$hashed")).thenReturn(true);
            when(sysConfigDomainService.getByConfigKey("password.expiry.days"))
                    .thenReturn(buildPasswordConfig("90"));
            when(userDomainService.isPasswordExpired(user, 90)).thenReturn(true);
            when(jwtUtil.generateToken(1L, "testuser")).thenReturn("jwt-token");
            when(sysUserRoleRepository.selectRoleIdsByUserId(1L)).thenReturn(List.of(1L));
            when(menuApplicationService.getUserPermissionsByUserId(1L)).thenReturn(buildUserPermission());

            LoginVO result = authApplicationService.login(buildLoginDTO("Test1234"));

            assertTrue(result.isPasswordExpired(), "密码已过期时 passwordExpired 应为 true");
        }

        @Test
        @DisplayName("登录时密码过期 — 仍允许登录并返回 Token")
        void login_passwordExpired_shouldStillReturnToken() {
            SysUser user = buildActiveUser();
            user.setPasswordUpdatedAt(LocalDateTime.now().minusDays(100));

            when(sysUserRepository.selectByUsername("testuser")).thenReturn(user);
            when(passwordEncoder.matches("Test1234", "$2a$10$hashed")).thenReturn(true);
            when(sysConfigDomainService.getByConfigKey("password.expiry.days"))
                    .thenReturn(buildPasswordConfig("90"));
            when(userDomainService.isPasswordExpired(user, 90)).thenReturn(true);
            when(jwtUtil.generateToken(1L, "testuser")).thenReturn("jwt-token");
            when(sysUserRoleRepository.selectRoleIdsByUserId(1L)).thenReturn(List.of(1L));
            when(menuApplicationService.getUserPermissionsByUserId(1L)).thenReturn(buildUserPermission());

            LoginVO result = authApplicationService.login(buildLoginDTO("Test1234"));

            assertNotNull(result.getToken(), "密码过期时仍应返回 Token");
            assertEquals("jwt-token", result.getToken());
        }

        @Test
        @DisplayName("配置密码永不过期（0天） — passwordExpired 应为 false")
        void login_passwordNeverExpires_shouldReturnFalse() {
            SysUser user = buildActiveUser();
            user.setPasswordUpdatedAt(LocalDateTime.now().minusDays(365));

            when(sysUserRepository.selectByUsername("testuser")).thenReturn(user);
            when(passwordEncoder.matches("Test1234", "$2a$10$hashed")).thenReturn(true);
            when(sysConfigDomainService.getByConfigKey("password.expiry.days"))
                    .thenReturn(buildPasswordConfig("0"));
            when(userDomainService.isPasswordExpired(user, 0)).thenReturn(false);
            when(jwtUtil.generateToken(1L, "testuser")).thenReturn("jwt-token");
            when(sysUserRoleRepository.selectRoleIdsByUserId(1L)).thenReturn(List.of(1L));
            when(menuApplicationService.getUserPermissionsByUserId(1L)).thenReturn(buildUserPermission());

            LoginVO result = authApplicationService.login(buildLoginDTO("Test1234"));

            assertFalse(result.isPasswordExpired(), "配置为0时密码永不过期");
        }

        @Test
        @DisplayName("读取密码过期天数配置异常 — 使用默认值90天，登录不报错")
        void login_configReadFails_shouldUseDefaultAndNotThrow() {
            SysUser user = buildActiveUser();
            user.setPasswordUpdatedAt(LocalDateTime.now().minusDays(10));

            when(sysUserRepository.selectByUsername("testuser")).thenReturn(user);
            when(passwordEncoder.matches("Test1234", "$2a$10$hashed")).thenReturn(true);
            when(sysConfigDomainService.getByConfigKey("password.expiry.days"))
                    .thenThrow(new BizException(ErrorCode.DATA_NOT_FOUND, "配置项不存在"));
            when(userDomainService.isPasswordExpired(user, 90)).thenReturn(false);
            when(jwtUtil.generateToken(1L, "testuser")).thenReturn("jwt-token");
            when(sysUserRoleRepository.selectRoleIdsByUserId(1L)).thenReturn(List.of(1L));
            when(menuApplicationService.getUserPermissionsByUserId(1L)).thenReturn(buildUserPermission());

            LoginVO result = authApplicationService.login(buildLoginDTO("Test1234"));

            assertNotNull(result.getToken(), "配置读取失败时登录不应报错");
            assertFalse(result.isPasswordExpired());
        }

        @Test
        @DisplayName("用户停用时登录 — 应返回 USER_INACTIVE 错误")
        void login_inactiveUser_shouldThrow() {
            SysUser user = buildActiveUser();
            user.setStatus(UserStatus.INACTIVE);

            when(sysUserRepository.selectByUsername("testuser")).thenReturn(user);

            BizException ex = assertThrows(BizException.class,
                    () -> authApplicationService.login(buildLoginDTO("Test1234")));
            assertEquals(ErrorCode.USER_INACTIVE.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("密码错误时登录 — 应返回 LOGIN_FAILED 错误")
        void login_wrongPassword_shouldThrow() {
            SysUser user = buildActiveUser();

            when(sysUserRepository.selectByUsername("testuser")).thenReturn(user);
            when(passwordEncoder.matches("WrongPwd1", "$2a$10$hashed")).thenReturn(false);

            BizException ex = assertThrows(BizException.class,
                    () -> authApplicationService.login(buildLoginDTO("WrongPwd1")));
            assertEquals(ErrorCode.LOGIN_FAILED.getCode(), ex.getCode());
        }
    }

    // ==================== 辅助方法 ====================

    private LoginDTO buildLoginDTO(String password) {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword(password);
        return dto;
    }

    private SysConfig buildPasswordConfig(String days) {
        SysConfig config = new SysConfig();
        config.setConfigKey("password.expiry.days");
        config.setConfigValue(days);
        return config;
    }

    private UserPermissionVO buildUserPermission() {
        UserPermissionVO vo = new UserPermissionVO();
        vo.setPermissions(List.of("system:user:create"));
        vo.setMenuTree(List.of());
        return vo;
    }
}
