package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.master.domain.model.ColorWay;
import com.jingwei.master.domain.repository.ColorWayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 颜色款仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ColorWayRepositoryImpl implements ColorWayRepository {

    private final ColorWayMapper colorWayMapper;

    @Override
    public ColorWay selectById(Long id) {
        return colorWayMapper.selectById(id);
    }

    @Override
    public List<ColorWay> selectBySpuId(Long spuId) {
        return colorWayMapper.selectList(
                new LambdaQueryWrapper<ColorWay>()
                        .eq(ColorWay::getSpuId, spuId)
                        .orderByAsc(ColorWay::getSortOrder));
    }

    @Override
    public boolean existsBySpuIdAndColorCode(Long spuId, String colorCode, Long excludeId) {
        LambdaQueryWrapper<ColorWay> wrapper = new LambdaQueryWrapper<ColorWay>()
                .eq(ColorWay::getSpuId, spuId)
                .eq(ColorWay::getColorCode, colorCode);
        if (excludeId != null) {
            wrapper.ne(ColorWay::getId, excludeId);
        }
        return colorWayMapper.selectCount(wrapper) > 0;
    }

    @Override
    public int insert(ColorWay colorWay) {
        return colorWayMapper.insert(colorWay);
    }

    @Override
    public int updateById(ColorWay colorWay) {
        return colorWayMapper.updateById(colorWay);
    }

    @Override
    public int deleteById(Long id) {
        return colorWayMapper.deleteById(id);
    }

    @Override
    public long countBySpuId(Long spuId) {
        return colorWayMapper.selectCount(
                new LambdaQueryWrapper<ColorWay>()
                        .eq(ColorWay::getSpuId, spuId));
    }
}
