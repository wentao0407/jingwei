package com.jingwei.cost.domain.repository;

import com.jingwei.cost.domain.model.CostProductionOrder;

/**
 * 生产订单成本归集仓储接口
 *
 * @author JingWei
 */
public interface CostProductionOrderRepository {

    /**
     * 插入成本归集记录
     *
     * @param cost 成本归集记录
     */
    void insert(CostProductionOrder cost);

    /**
     * 根据生产订单行ID查询成本归集
     *
     * @param productionOrderId 生产订单ID
     * @param productionLineId  生产订单行ID
     * @return 成本归集记录
     */
    CostProductionOrder selectByOrderLineId(Long productionOrderId, Long productionLineId);

    /**
     * 更新成本归集记录
     *
     * @param cost 成本归集记录
     * @return 影响行数
     */
    int updateById(CostProductionOrder cost);
}
