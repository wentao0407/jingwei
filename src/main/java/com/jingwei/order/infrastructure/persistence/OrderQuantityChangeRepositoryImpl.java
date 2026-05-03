package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.order.domain.model.OrderQuantityChange;
import com.jingwei.order.domain.repository.OrderQuantityChangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数量变更单仓库实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class OrderQuantityChangeRepositoryImpl implements OrderQuantityChangeRepository {

    private final OrderQuantityChangeMapper mapper;

    @Override
    public int insert(OrderQuantityChange change) {
        return mapper.insert(change);
    }

    @Override
    public OrderQuantityChange selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public int updateById(OrderQuantityChange change) {
        return mapper.updateById(change);
    }

    @Override
    public List<OrderQuantityChange> selectByOrderId(Long orderId) {
        return mapper.selectList(new LambdaQueryWrapper<OrderQuantityChange>()
                .eq(OrderQuantityChange::getOrderId, orderId)
                .orderByDesc(OrderQuantityChange::getCreatedAt));
    }

    @Override
    public List<OrderQuantityChange> selectByOrderLineId(Long orderLineId) {
        return mapper.selectList(new LambdaQueryWrapper<OrderQuantityChange>()
                .eq(OrderQuantityChange::getOrderLineId, orderLineId)
                .orderByDesc(OrderQuantityChange::getCreatedAt));
    }
}
