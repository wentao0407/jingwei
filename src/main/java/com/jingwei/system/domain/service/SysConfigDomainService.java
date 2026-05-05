package com.jingwei.system.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.domain.model.SysConfig;
import com.jingwei.system.domain.repository.SysConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 系统配置领域服务
 * <p>
 * 使用本地 ConcurrentHashMap 缓存所有配置项，启动时全量加载，
 * 查询时优先走缓存，修改时同步刷新缓存。不依赖 Redis。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigDomainService {

    /** 编码规则仓库引用（供外部使用） */
    private final SysConfigRepository sysConfigRepository;

    /** 本地配置缓存（key=configKey, value=SysConfig） */
    private final Map<String, SysConfig> configCache = new ConcurrentHashMap<>();

    /**
     * 启动时加载所有配置到缓存
     */
    @PostConstruct
    public void initCache() {
        try {
            List<SysConfig> allConfigs = sysConfigRepository.selectAll();
            configCache.clear();
            for (SysConfig config : allConfigs) {
                configCache.put(config.getConfigKey(), config);
            }
            log.info("系统配置缓存加载完成: 共{}项配置", configCache.size());
        } catch (Exception e) {
            log.warn("系统配置缓存加载失败，将使用数据库查询兜底: {}", e.getMessage());
        }
    }

    /**
     * 刷新缓存（全量重新加载）
     */
    public void refreshCache() {
        initCache();
    }

    /**
     * 创建配置项
     */
    public SysConfig createConfig(SysConfig config) {
        if (sysConfigRepository.existsByConfigKey(config.getConfigKey(), null)) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "配置键已存在");
        }

        try {
            sysConfigRepository.insert(config);
        } catch (DuplicateKeyException e) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "配置键已存在");
        }

        // 同步更新缓存
        configCache.put(config.getConfigKey(), config);
        log.info("创建配置项: key={}", config.getConfigKey());
        return config;
    }

    /**
     * 更新配置项
     * <p>
     * 修改时必须填写修改原因（remark）。
     * </p>
     */
    public SysConfig updateConfig(Long configId, SysConfig config) {
        SysConfig existing = sysConfigRepository.selectById(configId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "配置项不存在");
        }

        // 配置键唯一性校验
        if (config.getConfigKey() != null && !config.getConfigKey().equals(existing.getConfigKey())) {
            if (sysConfigRepository.existsByConfigKey(config.getConfigKey(), configId)) {
                throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "配置键已存在");
            }
        }

        // 修改时必须填写修改原因
        if (config.getRemark() == null || config.getRemark().isBlank()) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "修改配置项必须填写修改原因");
        }

        config.setId(configId);
        // 配置键不可修改
        config.setConfigKey(existing.getConfigKey());

        int rows = sysConfigRepository.updateById(config);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        // 同步更新缓存
        SysConfig updated = sysConfigRepository.selectById(configId);
        if (updated != null) {
            configCache.put(updated.getConfigKey(), updated);
        }

        log.info("更新配置项: id={}, key={}, needRestart={}", configId, existing.getConfigKey(),
                config.getNeedRestart());
        return updated;
    }

    /**
     * 根据配置键查询（优先走缓存）
     *
     * @throws BizException 配置项不存在时抛出
     */
    public SysConfig getByConfigKey(String configKey) {
        // 优先查缓存
        SysConfig config = configCache.get(configKey);
        if (config != null) {
            return config;
        }
        // 缓存未命中，查数据库并回填缓存
        config = sysConfigRepository.selectByConfigKey(configKey);
        if (config == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "配置项不存在: " + configKey);
        }
        configCache.put(configKey, config);
        return config;
    }

    /**
     * 根据配置键静默查询（不抛异常，返回 null）
     * <p>
     * 供内部模块使用（如库存预留读取过期天数配置）。
     * </p>
     */
    public SysConfig getByConfigKeyOrNull(String configKey) {
        SysConfig config = configCache.get(configKey);
        if (config != null) {
            return config;
        }
        config = sysConfigRepository.selectByConfigKey(configKey);
        if (config != null) {
            configCache.put(configKey, config);
        }
        return config;
    }

    /**
     * 按分组查询（从缓存过滤）
     */
    public List<SysConfig> listByGroup(String configGroup) {
        return configCache.values().stream()
                .filter(c -> configGroup.equals(c.getConfigGroup()))
                .collect(Collectors.toList());
    }

    /**
     * 查询全部配置（从缓存获取）
     */
    public List<SysConfig> listAll() {
        return List.copyOf(configCache.values());
    }
}
