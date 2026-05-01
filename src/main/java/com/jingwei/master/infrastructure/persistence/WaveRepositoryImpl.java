package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.master.domain.model.Wave;
import com.jingwei.master.domain.repository.WaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 波段仓库实现
 * <p>
 * 基于 MyBatis-Plus 实现 WaveRepository 接口。
 * 波段从属于季节，所有操作都需关联 seasonId。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class WaveRepositoryImpl implements WaveRepository {

    private final WaveMapper waveMapper;

    @Override
    public Wave selectById(Long id) {
        return waveMapper.selectById(id);
    }

    @Override
    public List<Wave> selectBySeasonId(Long seasonId) {
        return waveMapper.selectList(
                new LambdaQueryWrapper<Wave>()
                        .eq(Wave::getSeasonId, seasonId)
                        .orderByAsc(Wave::getSortOrder));
    }

    @Override
    public boolean existsBySeasonIdAndCode(Long seasonId, String code, Long excludeId) {
        LambdaQueryWrapper<Wave> wrapper = new LambdaQueryWrapper<Wave>()
                .eq(Wave::getSeasonId, seasonId)
                .eq(Wave::getCode, code);

        if (excludeId != null) {
            wrapper.ne(Wave::getId, excludeId);
        }

        return waveMapper.selectCount(wrapper) > 0;
    }

    @Override
    public int getMaxSortOrder(Long seasonId) {
        // 查询该季节下所有波段，取最大排序号
        // 数据量通常在 10 以内，全量查询无性能问题
        List<Wave> waves = waveMapper.selectList(
                new LambdaQueryWrapper<Wave>()
                        .eq(Wave::getSeasonId, seasonId)
                        .select(Wave::getSortOrder));

        return waves.stream()
                .mapToInt(Wave::getSortOrder)
                .max()
                .orElse(0);
    }

    @Override
    public int insert(Wave wave) {
        return waveMapper.insert(wave);
    }

    @Override
    public int updateById(Wave wave) {
        return waveMapper.updateById(wave);
    }

    @Override
    public int deleteById(Long id) {
        return waveMapper.deleteById(id);
    }

    @Override
    public long countBySeasonId(Long seasonId) {
        return waveMapper.selectCount(
                new LambdaQueryWrapper<Wave>()
                        .eq(Wave::getSeasonId, seasonId));
    }
}
