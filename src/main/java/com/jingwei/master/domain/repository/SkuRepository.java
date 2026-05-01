package com.jingwei.master.domain.repository;

import com.jingwei.master.domain.model.Sku;

import java.math.BigDecimal;
import java.util.List;

/**
 * SKU 仓库接口
 *
 * @author JingWei
 */
public interface SkuRepository {

    Sku selectById(Long id);

    List<Sku> selectBySpuId(Long spuId);

    List<Sku> selectByColorWayId(Long colorWayId);

    boolean existsByCode(String code);

    int insert(Sku sku);

    int updateById(Sku sku);

    int deleteById(Long id);

    long countBySpuId(Long spuId);

    /**
     * 批量更新指定 SPU 下所有 SKU 的某个价格字段
     *
     * @param spuId SPU ID
     * @param priceType 价格类型：costPrice/salePrice/wholesalePrice
     * @param price  价格值
     * @return 更新行数
     */
    int batchUpdatePrice(Long spuId, String priceType, BigDecimal price);

    /**
     * 批量更新指定颜色款下所有 SKU 的某个价格字段
     *
     * @param colorWayId 颜色款 ID
     * @param priceType  价格类型：costPrice/salePrice/wholesalePrice
     * @param price      价格值
     * @return 更新行数
     */
    int batchUpdatePriceByColorWay(Long colorWayId, String priceType, BigDecimal price);
}
