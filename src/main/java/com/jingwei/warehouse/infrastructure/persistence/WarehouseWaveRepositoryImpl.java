package com.jingwei.warehouse.infrastructure.persistence;

import com.jingwei.warehouse.domain.model.Wave;
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
    public int insert(Wave wave) {
        return waveMapper.insert(wave);
    }

    @Override
    public int updateById(Wave wave) {
        return waveMapper.updateById(wave);
    }
}
