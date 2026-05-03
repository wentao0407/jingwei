package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.order.domain.model.ProductionOrderLine;
import com.jingwei.order.domain.repository.ProductionOrderLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 生产订单行仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductionOrderLineRepositoryImpl implements ProductionOrderLineRepository {

    private final ProductionOrderLineMapper productionOrderLineMapper;

    @Override
    public List<ProductionOrderLine> selectByOrderId(Long orderId) {
        return productionOrderLineMapper.selectList(
                new LambdaQueryWrapper<ProductionOrderLine>()
                        .eq(ProductionOrderLine::getOrderId, orderId)
                        .orderByAsc(ProductionOrderLine::getLineNo));
    }

    @Override
    public int batchInsert(List<ProductionOrderLine> lines) {
        int count = 0;
        for (ProductionOrderLine line : lines) {
            count += productionOrderLineMapper.insert(line);
        }
        return count;
    }

    @Override
    public int deleteByOrderId(Long orderId) {
        return productionOrderLineMapper.delete(
                new LambdaQueryWrapper<ProductionOrderLine>()
                        .eq(ProductionOrderLine::getOrderId, orderId));
    }

    @Override
    public ProductionOrderLine selectById(Long id) {
        return productionOrderLineMapper.selectById(id);
    }

    @Override
    public int updateById(ProductionOrderLine line) {
        return productionOrderLineMapper.updateById(line);
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return productionOrderLineMapper.selectCount(
                new LambdaQueryWrapper<ProductionOrderLine>()
                        .eq(ProductionOrderLine::getOrderId, orderId)) > 0;
    }
}
