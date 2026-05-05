package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.inventory.domain.model.StocktakingOrder;
import com.jingwei.inventory.domain.model.StocktakingStatus;
import com.jingwei.inventory.domain.repository.StocktakingLineRepository;
import com.jingwei.inventory.domain.repository.StocktakingOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 盘点单仓库实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class StocktakingOrderRepositoryImpl implements StocktakingOrderRepository {

    private final StocktakingOrderMapper stocktakingOrderMapper;
    private final StocktakingLineRepository stocktakingLineRepository;

    @Override
    public StocktakingOrder selectById(Long id) {
        return stocktakingOrderMapper.selectById(id);
    }

    @Override
    public StocktakingOrder selectDetailById(Long id) {
        StocktakingOrder order = stocktakingOrderMapper.selectById(id);
        if (order != null) {
            order.setLines(stocktakingLineRepository.selectByStocktakingId(id));
        }
        return order;
    }

    @Override
    public IPage<StocktakingOrder> selectPage(Page<StocktakingOrder> page, StocktakingStatus status, Long warehouseId) {
        LambdaQueryWrapper<StocktakingOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(StocktakingOrder::getStatus, status);
        if (warehouseId != null) wrapper.eq(StocktakingOrder::getWarehouseId, warehouseId);
        wrapper.orderByDesc(StocktakingOrder::getCreatedAt);
        return stocktakingOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public int insert(StocktakingOrder order) {
        return stocktakingOrderMapper.insert(order);
    }

    @Override
    public int updateById(StocktakingOrder order) {
        return stocktakingOrderMapper.updateById(order);
    }

    @Override
    public int deleteById(Long id) {
        return stocktakingOrderMapper.deleteById(id);
    }
}
