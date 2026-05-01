package com.jingwei.system;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.domain.model.DataScope;
import com.jingwei.system.domain.repository.DataScopeRepository;
import com.jingwei.system.domain.repository.SysRoleRepository;
import com.jingwei.system.domain.service.DataScopeDomainService;
import com.jingwei.system.domain.service.SysConfigDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 数据权限 + 系统配置 领域服务单元测试
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class DataScopeAndConfigDomainServiceTest {

    // ==================== 数据权限测试 ====================

    @Nested
    @DisplayName("数据权限")
    class DataScopeTests {

        @Mock
        private DataScopeRepository dataScopeRepository;

        @Mock
        private SysRoleRepository sysRoleRepository;

        @InjectMocks
        private DataScopeDomainService dataScopeDomainService;

        @Test
        @DisplayName("配置数据权限 — 全量替换应先删后插")
        void configureDataScope_shouldDeleteThenInsert() {
            when(sysRoleRepository.existsById(1L)).thenReturn(true);

            DataScope scope = new DataScope();
            scope.setScopeType("WAREHOUSE");
            scope.setScopeValue("1,2,3");

            dataScopeDomainService.configureDataScope(1L, List.of(scope));

            verify(dataScopeRepository).deleteByRoleId(1L);
            verify(dataScopeRepository).insert(any(DataScope.class));
        }

        @Test
        @DisplayName("配置数据权限 — 角色不存在应抛异常")
        void configureDataScope_roleNotFound_shouldThrow() {
            when(sysRoleRepository.existsById(999L)).thenReturn(false);

            DataScope scope = new DataScope();
            scope.setScopeType("WAREHOUSE");
            scope.setScopeValue("ALL");

            BizException ex = assertThrows(BizException.class,
                    () -> dataScopeDomainService.configureDataScope(999L, List.of(scope)));
            assertTrue(ex.getMessage().contains("角色不存在"));
        }

        @Test
        @DisplayName("查询角色数据权限")
        void getByRoleId_shouldReturn() {
            DataScope scope1 = new DataScope();
            scope1.setScopeType("WAREHOUSE");
            scope1.setScopeValue("1,2");
            DataScope scope2 = new DataScope();
            scope2.setScopeType("DEPT");
            scope2.setScopeValue("ALL");

            when(dataScopeRepository.selectByRoleId(1L)).thenReturn(List.of(scope1, scope2));

            List<DataScope> result = dataScopeDomainService.getByRoleId(1L);
            assertEquals(2, result.size());
        }
    }

    // ==================== 系统配置测试 ====================

    @Nested
    @DisplayName("系统配置")
    class SysConfigTests {

        @Mock
        private com.jingwei.system.domain.repository.SysConfigRepository sysConfigRepository;

        @InjectMocks
        private SysConfigDomainService sysConfigDomainService;

        @Test
        @DisplayName("创建配置项 — 正常创建")
        void createConfig_shouldSucceed() {
            when(sysConfigRepository.existsByConfigKey("test.key", null)).thenReturn(false);
            when(sysConfigRepository.insert(any())).thenReturn(1);

            com.jingwei.system.domain.model.SysConfig config = new com.jingwei.system.domain.model.SysConfig();
            config.setConfigKey("test.key");
            config.setConfigValue("test-value");
            config.setConfigGroup("DEFAULT");
            config.setRemark("");

            assertDoesNotThrow(() -> sysConfigDomainService.createConfig(config));
        }

        @Test
        @DisplayName("创建配置项 — 键重复应抛异常")
        void createConfig_duplicateKey_shouldThrow() {
            when(sysConfigRepository.existsByConfigKey("test.key", null)).thenReturn(true);

            com.jingwei.system.domain.model.SysConfig config = new com.jingwei.system.domain.model.SysConfig();
            config.setConfigKey("test.key");
            config.setConfigValue("value");

            BizException ex = assertThrows(BizException.class,
                    () -> sysConfigDomainService.createConfig(config));
            assertTrue(ex.getMessage().contains("配置键已存在"));
        }

        @Test
        @DisplayName("更新配置项 — 必须填写修改原因")
        void updateConfig_noRemark_shouldThrow() {
            com.jingwei.system.domain.model.SysConfig existing = new com.jingwei.system.domain.model.SysConfig();
            existing.setId(1L);
            existing.setConfigKey("test.key");
            when(sysConfigRepository.selectById(1L)).thenReturn(existing);

            com.jingwei.system.domain.model.SysConfig update = new com.jingwei.system.domain.model.SysConfig();
            update.setConfigValue("new-value");
            update.setRemark("");

            BizException ex = assertThrows(BizException.class,
                    () -> sysConfigDomainService.updateConfig(1L, update));
            assertTrue(ex.getMessage().contains("修改原因"));
        }

        @Test
        @DisplayName("更新配置项 — 配置键不可修改")
        void updateConfig_keyImmutable() {
            com.jingwei.system.domain.model.SysConfig existing = new com.jingwei.system.domain.model.SysConfig();
            existing.setId(1L);
            existing.setConfigKey("original.key");
            when(sysConfigRepository.selectById(1L)).thenReturn(existing);
            when(sysConfigRepository.updateById(any())).thenReturn(1);
            when(sysConfigRepository.selectById(1L)).thenReturn(existing);

            com.jingwei.system.domain.model.SysConfig update = new com.jingwei.system.domain.model.SysConfig();
            update.setConfigValue("new-value");
            update.setRemark("业务需要");

            sysConfigDomainService.updateConfig(1L, update);

            verify(sysConfigRepository).updateById(argThat(c ->
                    c.getConfigKey().equals("original.key")));
        }

        @Test
        @DisplayName("更新配置项 — 不存在应抛异常")
        void updateConfig_notFound_shouldThrow() {
            when(sysConfigRepository.selectById(999L)).thenReturn(null);

            com.jingwei.system.domain.model.SysConfig update = new com.jingwei.system.domain.model.SysConfig();
            update.setConfigValue("new");
            update.setRemark("原因");

            BizException ex = assertThrows(BizException.class,
                    () -> sysConfigDomainService.updateConfig(999L, update));
            assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
        }
    }
}
