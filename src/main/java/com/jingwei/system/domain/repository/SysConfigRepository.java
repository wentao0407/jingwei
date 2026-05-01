package com.jingwei.system.domain.repository;

import com.jingwei.system.domain.model.SysConfig;

import java.util.List;

/**
 * 系统配置仓库接口
 *
 * @author JingWei
 */
public interface SysConfigRepository {

    SysConfig selectById(Long id);

    SysConfig selectByConfigKey(String configKey);

    List<SysConfig> selectByConfigGroup(String configGroup);

    List<SysConfig> selectAll();

    boolean existsByConfigKey(String configKey, Long excludeId);

    int insert(SysConfig sysConfig);

    int updateById(SysConfig sysConfig);
}
