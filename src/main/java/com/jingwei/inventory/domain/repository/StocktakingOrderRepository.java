package com.jingwei.inventory.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.inventory.domain.model.StocktakingOrder;
import com.jingwei.inventory.domain.model.StocktakingStatus;

/**
 * 盘点单仓库接口
 *
 * @author JingWei
 */
public interface StocktakingOrderRepository {

    StocktakingOrder selectById(Long id);

    StocktakingOrder selectDetailById(Long id);

    IPage<StocktakingOrder> selectPage(Page<StocktakingOrder> page, StocktakingStatus status, Long warehouseId);

    int insert(StocktakingOrder order);

    int updateById(StocktakingOrder order);

    int deleteById(Long id);
}
