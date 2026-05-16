package com.jingwei.warehouse.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.warehouse.domain.model.Wave;
import com.jingwei.warehouse.domain.model.WaveStatus;

/**
 * 波次仓库接口
 *
 * @author JingWei
 */
public interface WaveRepository {
    Wave selectById(Long id);
    Wave selectDetailById(Long id);
    Page<Wave> selectPage(Page<Wave> page, Long warehouseId, WaveStatus status, String waveNo);
    int insert(Wave wave);
    int updateById(Wave wave);
}
