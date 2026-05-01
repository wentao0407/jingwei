package com.jingwei.system.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.system.domain.model.SysConfig;
import com.jingwei.system.domain.repository.SysConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SysConfigRepositoryImpl implements SysConfigRepository {

    private final SysConfigMapper sysConfigMapper;

    @Override
    public SysConfig selectById(Long id) {
        return sysConfigMapper.selectById(id);
    }

    @Override
    public SysConfig selectByConfigKey(String configKey) {
        return sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, configKey));
    }

    @Override
    public List<SysConfig> selectByConfigGroup(String configGroup) {
        return sysConfigMapper.selectList(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigGroup, configGroup)
                        .orderByAsc(SysConfig::getConfigKey));
    }

    @Override
    public List<SysConfig> selectAll() {
        return sysConfigMapper.selectList(
                new LambdaQueryWrapper<SysConfig>()
                        .orderByAsc(SysConfig::getConfigGroup, SysConfig::getConfigKey));
    }

    @Override
    public boolean existsByConfigKey(String configKey, Long excludeId) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey);
        if (excludeId != null) {
            wrapper.ne(SysConfig::getId, excludeId);
        }
        return sysConfigMapper.selectCount(wrapper) > 0;
    }

    @Override
    public int insert(SysConfig sysConfig) {
        return sysConfigMapper.insert(sysConfig);
    }

    @Override
    public int updateById(SysConfig sysConfig) {
        return sysConfigMapper.updateById(sysConfig);
    }
}
