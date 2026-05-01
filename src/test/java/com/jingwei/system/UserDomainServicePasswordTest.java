package com.jingwei.system;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.domain.model.SysUser;
import com.jingwei.system.domain.model.UserStatus;
import com.jingwei.system.domain.repository.SysUserRepository;
import com.jingwei.system.domain.repository.SysUserRoleRepository;
import com.jingwei.system.domain.service.UserDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户领域服务单元测试 — 密码策略补全
 * <p>
 * 覆盖：创建用户时记录密码更新时间、修改密码、密码过期检查。
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class UserDomainServicePasswordTest {

    @Mock
    private SysUserRepository sysUserRepository;

    @Mock
    private SysUserRoleRepository sysUserRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserDomainService userDomainService;

    // ==================== 创建用户 ====================

    @Nested
    @DisplayName("创建用户 — 密码策略")
    class CreateUserTests {

        @Test
        @DisplayName("创建用户时应自动设置 passwordUpdatedAt")
        void createUser_shouldSetPasswordUpdatedAt() {
            when(sysUserRepository.existsByUsername("newuser")).thenReturn(false);
            when(passwordEncoder.encode("Test1234")).thenReturn("$2a$10$encoded");
            when(sysUserRepository.insert(any())).thenReturn(1);

            SysUser user = new SysUser();
            user.setUsername("newuser");
            user.setPassword("Test1234");

            SysUser result = userDomainService.createUser(user);

            assertNotNull(result.getPasswordUpdatedAt(), "passwordUpdatedAt 不应为 null");
            // 密码更新时间应该在当前时间前后1秒内
            assertTrue(result.getPasswordUpdatedAt().isAfter(LocalDateTime.now().minusSeconds(2)));
            verify(passwordEncoder).encode("Test1234");
        }

        @Test
        @DisplayName("创建用户时密码应BCrypt加密，数据库中不存明文")
        void createUser_shouldEncryptPassword() {
            when(sysUserRepository.existsByUsername("encuser")).thenReturn(false);
            when(passwordEncoder.encode("PlainPwd1")).thenReturn("$2a$10$hashed");
            when(sysUserRepository.insert(any())).thenReturn(1);

            SysUser user = new SysUser();
            user.setUsername("encuser");
            user.setPassword("PlainPwd1");

            userDomainService.createUser(user);

            assertEquals("$2a$10$hashed", user.getPassword(), "密码应为加密后的值");
        }
    }

    // ==================== 修改密码 ====================

    @Nested
    @DisplayName("修改密码")
    class ChangePasswordTests {

        @Test
        @DisplayName("修改密码 — 正常修改应更新密码和 passwordUpdatedAt")
        void changePassword_shouldUpdatePasswordAndTimestamp() {
            SysUser existing = new SysUser();
            existing.setId(1L);
            existing.setPassword("$2a$10$oldHash");
            existing.setPasswordUpdatedAt(LocalDateTime.now().minusDays(100));

            when(sysUserRepository.selectById(1L)).thenReturn(existing);
            when(passwordEncoder.matches("OldPwd123", "$2a$10$oldHash")).thenReturn(true);
            when(passwordEncoder.matches("NewPwd456", "$2a$10$oldHash")).thenReturn(false);
            when(passwordEncoder.encode("NewPwd456")).thenReturn("$2a$10$newHash");
            when(sysUserRepository.updateById(any())).thenReturn(1);

            userDomainService.changePassword(1L, "OldPwd123", "NewPwd456");

            assertEquals("$2a$10$newHash", existing.getPassword(), "密码应更新为新加密值");
            assertNotNull(existing.getPasswordUpdatedAt(), "passwordUpdatedAt 应更新");
            assertTrue(existing.getPasswordUpdatedAt().isAfter(LocalDateTime.now().minusSeconds(2)));
            verify(sysUserRepository).updateById(existing);
        }

        @Test
        @DisplayName("修改密码 — 旧密码不正确应抛异常")
        void changePassword_wrongOldPassword_shouldThrow() {
            SysUser existing = new SysUser();
            existing.setId(1L);
            existing.setPassword("$2a$10$oldHash");

            when(sysUserRepository.selectById(1L)).thenReturn(existing);
            when(passwordEncoder.matches("WrongPwd1", "$2a$10$oldHash")).thenReturn(false);

            BizException ex = assertThrows(BizException.class,
                    () -> userDomainService.changePassword(1L, "WrongPwd1", "NewPwd456"));
            assertEquals(ErrorCode.OLD_PASSWORD_MISMATCH.getCode(), ex.getCode());
            verify(sysUserRepository, never()).updateById(any());
        }

        @Test
        @DisplayName("修改密码 — 新密码与旧密码相同应抛异常")
        void changePassword_sameAsOld_shouldThrow() {
            SysUser existing = new SysUser();
            existing.setId(1L);
            existing.setPassword("$2a$10$oldHash");

            when(sysUserRepository.selectById(1L)).thenReturn(existing);
            when(passwordEncoder.matches("SamePwd12", "$2a$10$oldHash")).thenReturn(true);
            when(passwordEncoder.matches("SamePwd12", "$2a$10$oldHash")).thenReturn(true);

            BizException ex = assertThrows(BizException.class,
                    () -> userDomainService.changePassword(1L, "SamePwd12", "SamePwd12"));
            assertEquals(ErrorCode.PASSWORD_SAME_AS_OLD.getCode(), ex.getCode());
            verify(sysUserRepository, never()).updateById(any());
        }

        @Test
        @DisplayName("修改密码 — 用户不存在应抛异常")
        void changePassword_userNotFound_shouldThrow() {
            when(sysUserRepository.selectById(999L)).thenReturn(null);

            BizException ex = assertThrows(BizException.class,
                    () -> userDomainService.changePassword(999L, "OldPwd123", "NewPwd456"));
            assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
        }
    }

    // ==================== 密码过期检查 ====================

    @Nested
    @DisplayName("密码过期检查")
    class PasswordExpiryTests {

        @Test
        @DisplayName("密码未过期 — 返回 false")
        void isPasswordExpired_notExpired_shouldReturnFalse() {
            SysUser user = new SysUser();
            user.setPasswordUpdatedAt(LocalDateTime.now().minusDays(30));

            boolean expired = userDomainService.isPasswordExpired(user, 90);

            assertFalse(expired, "30天内修改的密码不应过期");
        }

        @Test
        @DisplayName("密码已过期 — 超过90天未修改应返回 true")
        void isPasswordExpired_expired_shouldReturnTrue() {
            SysUser user = new SysUser();
            user.setPasswordUpdatedAt(LocalDateTime.now().minusDays(91));

            boolean expired = userDomainService.isPasswordExpired(user, 90);

            assertTrue(expired, "超过90天未修改的密码应过期");
        }

        @Test
        @DisplayName("密码刚好在过期边界 — 90天整不应过期")
        void isPasswordExpired_exactBoundary_shouldNotExpire() {
            // 90天前修改的密码，在90天过期策略下不应过期（需要超过90天才算过期）
            SysUser user = new SysUser();
            user.setPasswordUpdatedAt(LocalDateTime.now().minusDays(90).plusSeconds(1));

            boolean expired = userDomainService.isPasswordExpired(user, 90);

            assertFalse(expired, "刚好90天的密码不应过期，需超过90天才过期");
        }

        @Test
        @DisplayName("配置为0表示密码永不过期")
        void isPasswordExpired_zeroDays_shouldNeverExpire() {
            SysUser user = new SysUser();
            user.setPasswordUpdatedAt(LocalDateTime.now().minusDays(365));

            boolean expired = userDomainService.isPasswordExpired(user, 0);

            assertFalse(expired, "配置为0表示永不过期");
        }

        @Test
        @DisplayName("配置为负数表示密码永不过期")
        void isPasswordExpired_negativeDays_shouldNeverExpire() {
            SysUser user = new SysUser();
            user.setPasswordUpdatedAt(LocalDateTime.now().minusDays(365));

            boolean expired = userDomainService.isPasswordExpired(user, -1);

            assertFalse(expired, "配置为负数表示永不过期");
        }

        @Test
        @DisplayName("passwordUpdatedAt 为 null（老数据）— 应视为已过期")
        void isPasswordExpired_nullUpdatedAt_shouldReturnTrue() {
            SysUser user = new SysUser();
            user.setPasswordUpdatedAt(null);

            boolean expired = userDomainService.isPasswordExpired(user, 90);

            assertTrue(expired, "passwordUpdatedAt 为 null 的老数据应视为已过期");
        }
    }
}
