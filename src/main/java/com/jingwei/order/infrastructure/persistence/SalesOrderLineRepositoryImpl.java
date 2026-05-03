package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.order.domain.model.SalesOrderLine;
import com.jingwei.order.domain.repository.SalesOrderLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 销售订单行仓库实现
 * <p>
 * 基于 MyBatis-Plus 实现 SalesOrderLineRepository 接口。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SalesOrderLineRepositoryImpl implements SalesOrderLineRepository {

    private final SalesOrderLineMapper salesOrderLineMapper;

    @Override
    public List<SalesOrderLine> selectByOrderId(Long orderId) {
        return salesOrderLineMapper.selectList(
                new LambdaQueryWrapper<SalesOrderLine>()
                        .eq(SalesOrderLine::getOrderId, orderId)
                        .orderByAsc(SalesOrderLine::getLineNo));
    }

    @Override
    public int batchInsert(List<SalesOrderLine> lines) {
        // MyBatis-Plus 没有原生批量插入，逐条插入（订单行数量通常较少，性能可接受）
        int count = 0;
        for (SalesOrderLine line : lines) {
            count += salesOrderLineMapper.insert(line);
        }
        return count;
    }

    @Override
    public int deleteByOrderId(Long orderId) {
        return salesOrderLineMapper.delete(
                new LambdaQueryWrapper<SalesOrderLine>()
                        .eq(SalesOrderLine::getOrderId, orderId));
    }

    @Override
    public boolean existsBySpuAndColor(Long orderId, Long spuId, Long colorWayId, Long excludeLineId) {
        LambdaQueryWrapper<SalesOrderLine> wrapper = new LambdaQueryWrapper<SalesOrderLine>()
                .eq(SalesOrderLine::getOrderId, orderId)
                .eq(SalesOrderLine::getSpuId, spuId)
                .eq(SalesOrderLine::getColorWayId, colorWayId);
        if (excludeLineId != null) {
            wrapper.ne(SalesOrderLine::getId, excludeLineId);
        }
        return salesOrderLineMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return salesOrderLineMapper.selectCount(
                new LambdaQueryWrapper<SalesOrderLine>()
                        .eq(SalesOrderLine::getOrderId, orderId)) > 0;
    }
}
