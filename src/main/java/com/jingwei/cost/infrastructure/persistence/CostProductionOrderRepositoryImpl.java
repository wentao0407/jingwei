package com.jingwei.cost.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.cost.domain.model.CostProductionOrder;
import com.jingwei.cost.domain.repository.CostProductionOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 生产订单成本归集仓储实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class CostProductionOrderRepositoryImpl implements CostProductionOrderRepository {

    private final CostProductionOrderMapper costProductionOrderMapper;

    @Override
    public void insert(CostProductionOrder cost) {
        costProductionOrderMapper.insert(cost);
    }

    @Override
    public CostProductionOrder selectByOrderLineId(Long productionOrderId, Long productionLineId) {
        LambdaQueryWrapper<CostProductionOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CostProductionOrder::getProductionOrderId, productionOrderId);
        wrapper.eq(CostProductionOrder::getProductionLineId, productionLineId);
        return costProductionOrderMapper.selectOne(wrapper);
    }

    @Override
    public int updateById(CostProductionOrder cost) {
        return costProductionOrderMapper.updateById(cost);
    }
}
