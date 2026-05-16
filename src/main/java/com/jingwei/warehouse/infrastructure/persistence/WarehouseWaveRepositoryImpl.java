package com.jingwei.warehouse.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.warehouse.domain.model.Wave;
import com.jingwei.warehouse.domain.model.WaveStatus;
import com.jingwei.warehouse.domain.repository.PickListRepository;
import com.jingwei.warehouse.domain.repository.WaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WarehouseWaveRepositoryImpl implements WaveRepository {

    private final WarehouseWaveMapper waveMapper;
    private final PickListRepository pickListRepository;

    @Override
    public Wave selectById(Long id) {
        return waveMapper.selectById(id);
    }

    @Override
    public Wave selectDetailById(Long id) {
        Wave wave = waveMapper.selectById(id);
        if (wave != null) {
            wave.setPickLists(pickListRepository.selectByWaveId(id));
        }
        return wave;
    }

    @Override
    public Page<Wave> selectPage(Page<Wave> page, Long warehouseId, WaveStatus status, String waveNo) {
        LambdaQueryWrapper<Wave> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(Wave::getWarehouseId, warehouseId);
        }
        if (status != null) {
            wrapper.eq(Wave::getStatus, status);
        }
        if (waveNo != null && !waveNo.isBlank()) {
            wrapper.like(Wave::getWaveNo, waveNo.trim());
        }
        wrapper.orderByDesc(Wave::getCreatedAt);
        return waveMapper.selectPage(page, wrapper);
    }

    @Override
    public int insert(Wave wave) {
        return waveMapper.insert(wave);
    }

    @Override
    public int updateById(Wave wave) {
        return waveMapper.updateById(wave);
    }
}
