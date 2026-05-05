package com.jingwei.warehouse.domain.repository;

import com.jingwei.warehouse.domain.model.Wave;

/**
 * 波次仓库接口
 *
 * @author JingWei
 */
public interface WaveRepository {
    Wave selectById(Long id);
    Wave selectDetailById(Long id);
    int insert(Wave wave);
    int updateById(Wave wave);
}
