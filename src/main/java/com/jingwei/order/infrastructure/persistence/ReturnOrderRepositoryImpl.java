package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.order.domain.model.ReturnOrder;
import com.jingwei.order.domain.model.ReturnStatus;
import com.jingwei.order.domain.repository.ReturnOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 退货单仓储实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class ReturnOrderRepositoryImpl implements ReturnOrderRepository {

    private final ReturnOrderMapper returnOrderMapper;

    @Override
    public void insert(ReturnOrder returnOrder) {
        returnOrderMapper.insert(returnOrder);
    }

    @Override
    public ReturnOrder selectById(Long id) {
        return returnOrderMapper.selectById(id);
    }

    @Override
    public int updateById(ReturnOrder returnOrder) {
        return returnOrderMapper.updateById(returnOrder);
    }

    @Override
    public Page<ReturnOrder> selectPage(Page<ReturnOrder> page, Long customerId, ReturnStatus status) {
        LambdaQueryWrapper<ReturnOrder> wrapper = new LambdaQueryWrapper<>();
        if (customerId != null) {
            wrapper.eq(ReturnOrder::getCustomerId, customerId);
        }
        if (status != null) {
            wrapper.eq(ReturnOrder::getStatus, status);
        }
        wrapper.orderByDesc(ReturnOrder::getCreatedAt);
        return returnOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public ReturnOrder selectByReturnNo(String returnNo) {
        LambdaQueryWrapper<ReturnOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReturnOrder::getReturnNo, returnNo);
        return returnOrderMapper.selectOne(wrapper);
    }
}
