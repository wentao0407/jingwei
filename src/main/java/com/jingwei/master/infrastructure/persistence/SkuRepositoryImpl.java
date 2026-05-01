package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jingwei.master.domain.model.Sku;
import com.jingwei.master.domain.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * SKU 仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SkuRepositoryImpl implements SkuRepository {

    private final SkuMapper skuMapper;

    @Override
    public Sku selectById(Long id) {
        return skuMapper.selectById(id);
    }

    @Override
    public List<Sku> selectBySpuId(Long spuId) {
        return skuMapper.selectList(
                new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getSpuId, spuId)
                        .orderByAsc(Sku::getCode));
    }

    @Override
    public List<Sku> selectByColorWayId(Long colorWayId) {
        return skuMapper.selectList(
                new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getColorWayId, colorWayId)
                        .orderByAsc(Sku::getCode));
    }

    @Override
    public boolean existsByCode(String code) {
        return skuMapper.selectCount(
                new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getCode, code)) > 0;
    }

    @Override
    public int insert(Sku sku) {
        return skuMapper.insert(sku);
    }

    @Override
    public int updateById(Sku sku) {
        return skuMapper.updateById(sku);
    }

    @Override
    public int deleteById(Long id) {
        return skuMapper.deleteById(id);
    }

    @Override
    public long countBySpuId(Long spuId) {
        return skuMapper.selectCount(
                new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getSpuId, spuId));
    }

    @Override
    public int batchUpdatePrice(Long spuId, String priceType, BigDecimal price) {
        // 使用 LambdaUpdateWrapper 批量更新指定 SPU 下所有未删除 SKU 的某个价格字段
        // priceType 只允许 costPrice/salePrice/wholesalePrice，由 DomainService 校验
        LambdaUpdateWrapper<Sku> wrapper = new LambdaUpdateWrapper<Sku>()
                .eq(Sku::getSpuId, spuId);

        switch (priceType) {
            case "costPrice" -> wrapper.set(Sku::getCostPrice, price);
            case "salePrice" -> wrapper.set(Sku::getSalePrice, price);
            case "wholesalePrice" -> wrapper.set(Sku::getWholesalePrice, price);
            default -> throw new IllegalArgumentException("不支持的价格类型: " + priceType);
        }

        return skuMapper.update(null, wrapper);
    }

    @Override
    public int batchUpdatePriceByColorWay(Long colorWayId, String priceType, BigDecimal price) {
        // 使用 LambdaUpdateWrapper 批量更新指定颜色款下所有未删除 SKU 的某个价格字段
        // priceType 只允许 costPrice/salePrice/wholesalePrice，由 DomainService 校验
        LambdaUpdateWrapper<Sku> wrapper = new LambdaUpdateWrapper<Sku>()
                .eq(Sku::getColorWayId, colorWayId);

        switch (priceType) {
            case "costPrice" -> wrapper.set(Sku::getCostPrice, price);
            case "salePrice" -> wrapper.set(Sku::getSalePrice, price);
            case "wholesalePrice" -> wrapper.set(Sku::getWholesalePrice, price);
            default -> throw new IllegalArgumentException("不支持的价格类型: " + priceType);
        }

        return skuMapper.update(null, wrapper);
    }
}
