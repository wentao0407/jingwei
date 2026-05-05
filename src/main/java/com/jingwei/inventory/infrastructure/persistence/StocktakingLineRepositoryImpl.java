package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.inventory.domain.model.StocktakingLine;
import com.jingwei.inventory.domain.repository.StocktakingLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 盘点行仓库实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class StocktakingLineRepositoryImpl implements StocktakingLineRepository {

    private final StocktakingLineMapper stocktakingLineMapper;

    @Override
    public List<StocktakingLine> selectByStocktakingId(Long stocktakingId) {
        return stocktakingLineMapper.selectList(
                new LambdaQueryWrapper<StocktakingLine>()
                        .eq(StocktakingLine::getStocktakingId, stocktakingId)
                        .orderByAsc(StocktakingLine::getId));
    }

    @Override
    public int batchInsert(List<StocktakingLine> lines) {
        int count = 0;
        for (StocktakingLine line : lines) {
            count += stocktakingLineMapper.insert(line);
        }
        return count;
    }

    @Override
    public int updateById(StocktakingLine line) {
        return stocktakingLineMapper.updateById(line);
    }

    @Override
    public int deleteByStocktakingId(Long stocktakingId) {
        return stocktakingLineMapper.delete(
                new LambdaQueryWrapper<StocktakingLine>()
                        .eq(StocktakingLine::getStocktakingId, stocktakingId));
    }
}
