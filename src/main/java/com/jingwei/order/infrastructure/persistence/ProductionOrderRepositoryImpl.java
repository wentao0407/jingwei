package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.order.domain.model.ProductionOrder;
import com.jingwei.order.domain.model.ProductionOrderLine;
import com.jingwei.order.domain.repository.ProductionOrderLineRepository;
import com.jingwei.order.domain.repository.ProductionOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 生产订单仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductionOrderRepositoryImpl implements ProductionOrderRepository {

    private final ProductionOrderMapper productionOrderMapper;
    private final ProductionOrderLineRepository productionOrderLineRepository;

    @Override
    public ProductionOrder selectById(Long id) {
        return productionOrderMapper.selectById(id);
    }

    @Override
    public ProductionOrder selectDetailById(Long id) {
        ProductionOrder order = productionOrderMapper.selectById(id);
        if (order != null) {
            List<ProductionOrderLine> lines = productionOrderLineRepository.selectByOrderId(id);
            order.setLines(lines);
        }
        return order;
    }

    @Override
    public IPage<ProductionOrder> selectPage(IPage<ProductionOrder> page,
                                              String status, String orderNo,
                                              String planDateStart, String planDateEnd) {
        LambdaQueryWrapper<ProductionOrder> wrapper = new LambdaQueryWrapper<ProductionOrder>()
                .eq(status != null && !status.isEmpty(), ProductionOrder::getStatus, status)
                .like(orderNo != null && !orderNo.isBlank(), ProductionOrder::getOrderNo, orderNo)
                .ge(planDateStart != null && !planDateStart.isEmpty(), ProductionOrder::getPlanDate, planDateStart)
                .le(planDateEnd != null && !planDateEnd.isEmpty(), ProductionOrder::getPlanDate, planDateEnd)
                .orderByDesc(ProductionOrder::getCreatedAt);
        return productionOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean existsByOrderNo(String orderNo) {
        return productionOrderMapper.selectCount(
                new LambdaQueryWrapper<ProductionOrder>()
                        .eq(ProductionOrder::getOrderNo, orderNo)) > 0;
    }

    @Override
    public int insert(ProductionOrder order) {
        return productionOrderMapper.insert(order);
    }

    @Override
    public int updateById(ProductionOrder order) {
        return productionOrderMapper.updateById(order);
    }

    @Override
    public int deleteById(Long id) {
        return productionOrderMapper.deleteById(id);
    }
}
