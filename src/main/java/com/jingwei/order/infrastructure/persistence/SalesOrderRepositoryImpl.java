package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.order.domain.model.SalesOrder;
import com.jingwei.order.domain.model.SalesOrderLine;
import com.jingwei.order.domain.model.SalesOrderStatus;
import com.jingwei.order.domain.repository.SalesOrderLineRepository;
import com.jingwei.order.domain.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 销售订单仓库实现
 * <p>
 * 基于 MyBatis-Plus 实现 SalesOrderRepository 接口。
 * selectDetailById 方法额外查询订单行数据并填充到 order.lines 中。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SalesOrderRepositoryImpl implements SalesOrderRepository {

    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderLineRepository salesOrderLineRepository;

    @Override
    public SalesOrder selectById(Long id) {
        return salesOrderMapper.selectById(id);
    }

    @Override
    public SalesOrder selectDetailById(Long id) {
        SalesOrder order = salesOrderMapper.selectById(id);
        if (order != null) {
            List<SalesOrderLine> lines = salesOrderLineRepository.selectByOrderId(id);
            order.setLines(lines);
        }
        return order;
    }

    @Override
    public IPage<SalesOrder> selectPage(IPage<SalesOrder> page,
                                         String status, Long customerId, Long seasonId,
                                         String orderNo,
                                         String orderDateStart, String orderDateEnd) {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<SalesOrder>()
                .eq(status != null && !status.isEmpty(), SalesOrder::getStatus, status)
                .eq(customerId != null, SalesOrder::getCustomerId, customerId)
                .eq(seasonId != null, SalesOrder::getSeasonId, seasonId)
                .like(orderNo != null && !orderNo.isBlank(), SalesOrder::getOrderNo, orderNo)
                .ge(orderDateStart != null && !orderDateStart.isEmpty(), SalesOrder::getOrderDate, orderDateStart)
                .le(orderDateEnd != null && !orderDateEnd.isEmpty(), SalesOrder::getOrderDate, orderDateEnd)
                .orderByDesc(SalesOrder::getCreatedAt);
        return salesOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean existsByOrderNo(String orderNo) {
        return salesOrderMapper.selectCount(
                new LambdaQueryWrapper<SalesOrder>()
                        .eq(SalesOrder::getOrderNo, orderNo)) > 0;
    }

    @Override
    public int insert(SalesOrder order) {
        return salesOrderMapper.insert(order);
    }

    @Override
    public int updateById(SalesOrder order) {
        return salesOrderMapper.updateById(order);
    }

    @Override
    public int deleteById(Long id) {
        return salesOrderMapper.deleteById(id);
    }
}
