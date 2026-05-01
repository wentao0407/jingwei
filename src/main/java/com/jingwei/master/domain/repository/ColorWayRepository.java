package com.jingwei.master.domain.repository;

import com.jingwei.master.domain.model.ColorWay;

import java.util.List;

/**
 * 颜色款仓库接口
 *
 * @author JingWei
 */
public interface ColorWayRepository {

    ColorWay selectById(Long id);

    List<ColorWay> selectBySpuId(Long spuId);

    boolean existsBySpuIdAndColorCode(Long spuId, String colorCode, Long excludeId);

    int insert(ColorWay colorWay);

    int updateById(ColorWay colorWay);

    int deleteById(Long id);

    long countBySpuId(Long spuId);
}
