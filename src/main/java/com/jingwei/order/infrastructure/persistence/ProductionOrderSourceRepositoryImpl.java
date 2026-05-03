package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.order.domain.model.ProductionOrderSource;
import com.jingwei.order.domain.repository.ProductionOrderSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产订单与销售订单关联仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductionOrderSourceRepositoryImpl implements ProductionOrderSourceRepository {

    private final ProductionOrderSourceMapper productionOrderSourceMapper;

    @Override
    public List<ProductionOrderSource> selectByProductionOrderId(Long productionOrderId) {
        return productionOrderSourceMapper.selectList(
                new LambdaQueryWrapper<ProductionOrderSource>()
                        .eq(ProductionOrderSource::getProductionOrderId, productionOrderId));
    }

    @Override
    public List<ProductionOrderSource> selectBySalesOrderId(Long salesOrderId) {
        return productionOrderSourceMapper.selectList(
                new LambdaQueryWrapper<ProductionOrderSource>()
                        .eq(ProductionOrderSource::getSalesOrderId, salesOrderId));
    }

    @Override
    public int batchInsert(List<ProductionOrderSource> sources) {
        int count = 0;
        for (ProductionOrderSource source : sources) {
            if (source.getCreatedAt() == null) {
                source.setCreatedAt(LocalDateTime.now());
            }
            count += productionOrderSourceMapper.insert(source);
        }
        return count;
    }

    @Override
    public int deleteByProductionOrderId(Long productionOrderId) {
        return productionOrderSourceMapper.delete(
                new LambdaQueryWrapper<ProductionOrderSource>()
                        .eq(ProductionOrderSource::getProductionOrderId, productionOrderId));
    }
}
