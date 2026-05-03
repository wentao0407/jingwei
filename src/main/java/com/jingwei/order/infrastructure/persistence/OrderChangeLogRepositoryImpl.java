package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.order.domain.model.OrderChangeLog;
import com.jingwei.order.domain.repository.OrderChangeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单变更日志仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderChangeLogRepositoryImpl implements OrderChangeLogRepository {

    private final OrderChangeLogMapper orderChangeLogMapper;

    @Override
    public int insert(OrderChangeLog changeLog) {
        return orderChangeLogMapper.insert(changeLog);
    }

    @Override
    public List<OrderChangeLog> selectByOrder(String orderType, Long orderId) {
        return orderChangeLogMapper.selectList(
                new LambdaQueryWrapper<OrderChangeLog>()
                        .eq(OrderChangeLog::getOrderType, orderType)
                        .eq(OrderChangeLog::getOrderId, orderId)
                        .orderByDesc(OrderChangeLog::getOperatedAt));
    }
}
