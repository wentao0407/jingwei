package com.jingwei.system.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.system.domain.model.SysConfig;
import com.jingwei.system.domain.repository.SysConfigRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统配置领域服务
 *
 * @author JingWei
 */
@Getter
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigDomainService {

    /**
     * -- GETTER --
     *  获取仓库引用
     */
    private final SysConfigRepository sysConfigRepository;

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

        log.info("更新配置项: id={}, key={}, needRestart={}", configId, existing.getConfigKey(),
                config.getNeedRestart());
        return sysConfigRepository.selectById(configId);
    }

    /**
     * 根据配置键查询
     */
    public SysConfig getByConfigKey(String configKey) {
        SysConfig config = sysConfigRepository.selectByConfigKey(configKey);
        if (config == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "配置项不存在: " + configKey);
        }
        return config;
    }

    /**
     * 按分组查询
     */
    public List<SysConfig> listByGroup(String configGroup) {
        return sysConfigRepository.selectByConfigGroup(configGroup);
    }

    /**
     * 查询全部配置
     */
    public List<SysConfig> listAll() {
        return sysConfigRepository.selectAll();
    }
}
