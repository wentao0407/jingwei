package com.jingwei.inventory.domain.repository;

import com.jingwei.inventory.domain.model.StocktakingLine;

import java.util.List;

/**
 * 盘点行仓库接口
 *
 * @author JingWei
 */
public interface StocktakingLineRepository {

    List<StocktakingLine> selectByStocktakingId(Long stocktakingId);

    int batchInsert(List<StocktakingLine> lines);

    int updateById(StocktakingLine line);

    int deleteByStocktakingId(Long stocktakingId);
}
