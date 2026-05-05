package com.jingwei.system.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.system.domain.model.SysConfig;
import com.jingwei.system.domain.repository.SysConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 系统配置缓存单元测试
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class SysConfigCacheTest {

    @Mock
    private SysConfigRepository sysConfigRepository;

    private SysConfigDomainService service;

    @BeforeEach
    void setUp() {
        service = new SysConfigDomainService(sysConfigRepository);
    }

    private SysConfig buildConfig(Long id, String key, String value, String group) {
        SysConfig config = new SysConfig();
        config.setId(id);
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigGroup(group);
        config.setDescription("测试配置");
        return config;
    }

    @Nested
    @DisplayName("缓存加载")
    class CacheInitTests {

        @Test
        @DisplayName("启动时加载所有配置到缓存")
        void initCache_shouldLoadAll() {
            SysConfig c1 = buildConfig(1L, "key1", "value1", "DEFAULT");
            SysConfig c2 = buildConfig(2L, "key2", "value2", "INVENTORY");
            when(sysConfigRepository.selectAll()).thenReturn(List.of(c1, c2));

            service.initCache();

            assertEquals(2, service.getConfigCache().size());
            assertNotNull(service.getConfigCache().get("key1"));
            assertNotNull(service.getConfigCache().get("key2"));
        }
    }

    @Nested
    @DisplayName("缓存查询")
    class CacheQueryTests {

        @Test
        @DisplayName("getByConfigKey — 缓存命中时不查数据库")
        void getByConfigKey_cacheHit_shouldNotQueryDb() {
            SysConfig config = buildConfig(1L, "test.key", "100", "DEFAULT");
            when(sysConfigRepository.selectAll()).thenReturn(List.of(config));
            service.initCache();

            SysConfig result = service.getByConfigKey("test.key");

            assertEquals("100", result.getConfigValue());
            // 不应再调用数据库
            verify(sysConfigRepository, never()).selectByConfigKey(any());
        }

        @Test
        @DisplayName("getByConfigKey — 缓存未命中时查数据库并回填")
        void getByConfigKey_cacheMiss_shouldQueryAndBackfill() {
            SysConfig config = buildConfig(1L, "miss.key", "200", "DEFAULT");
            when(sysConfigRepository.selectAll()).thenReturn(List.of());
            service.initCache();
            when(sysConfigRepository.selectByConfigKey("miss.key")).thenReturn(config);

            SysConfig result = service.getByConfigKey("miss.key");

            assertEquals("200", result.getConfigValue());
            // 第二次查询应走缓存
            SysConfig result2 = service.getByConfigKey("miss.key");
            assertEquals("200", result2.getConfigValue());
            verify(sysConfigRepository, times(1)).selectByConfigKey("miss.key");
        }

        @Test
        @DisplayName("getByConfigKey — 配置不存在时抛异常")
        void getByConfigKey_notFound_shouldThrow() {
            when(sysConfigRepository.selectAll()).thenReturn(List.of());
            service.initCache();
            when(sysConfigRepository.selectByConfigKey("non.exist")).thenReturn(null);

            assertThrows(BizException.class, () -> service.getByConfigKey("non.exist"));
        }

        @Test
        @DisplayName("getByConfigKeyOrNull — 配置不存在时返回 null")
        void getByConfigKeyOrNull_notFound_shouldReturnNull() {
            when(sysConfigRepository.selectAll()).thenReturn(List.of());
            service.initCache();
            when(sysConfigRepository.selectByConfigKey("non.exist")).thenReturn(null);

            assertNull(service.getByConfigKeyOrNull("non.exist"));
        }

        @Test
        @DisplayName("listByGroup — 从缓存过滤")
        void listByGroup_shouldFilterFromCache() {
            SysConfig c1 = buildConfig(1L, "k1", "v1", "INVENTORY");
            SysConfig c2 = buildConfig(2L, "k2", "v2", "INVENTORY");
            SysConfig c3 = buildConfig(3L, "k3", "v3", "PASSWORD");
            when(sysConfigRepository.selectAll()).thenReturn(List.of(c1, c2, c3));
            service.initCache();

            List<SysConfig> result = service.listByGroup("INVENTORY");

            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("缓存刷新")
    class CacheRefreshTests {

        @Test
        @DisplayName("创建配置后缓存自动更新")
        void createConfig_shouldUpdateCache() {
            when(sysConfigRepository.selectAll()).thenReturn(List.of());
            service.initCache();
            when(sysConfigRepository.existsByConfigKey("new.key", null)).thenReturn(false);
            when(sysConfigRepository.insert(any())).thenReturn(1);

            SysConfig newConfig = buildConfig(null, "new.key", "new.value", "DEFAULT");
            service.createConfig(newConfig);

            assertEquals(1, service.getConfigCache().size());
            assertNotNull(service.getConfigCache().get("new.key"));
        }

        @Test
        @DisplayName("更新配置后缓存自动刷新")
        void updateConfig_shouldRefreshCache() {
            SysConfig existing = buildConfig(1L, "test.key", "old", "DEFAULT");
            when(sysConfigRepository.selectAll()).thenReturn(List.of(existing));
            service.initCache();

            SysConfig updated = buildConfig(1L, "test.key", "new", "DEFAULT");
            updated.setRemark("修改原因");
            when(sysConfigRepository.selectById(1L)).thenReturn(existing).thenReturn(updated);
            when(sysConfigRepository.updateById(any())).thenReturn(1);

            service.updateConfig(1L, updated);

            assertEquals("new", service.getConfigCache().get("test.key").getConfigValue());
        }
    }
}
