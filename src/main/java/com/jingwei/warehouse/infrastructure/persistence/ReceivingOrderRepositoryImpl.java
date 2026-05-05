package com.jingwei.warehouse.infrastructure.persistence;

import com.jingwei.warehouse.domain.model.ReceivingOrder;
import com.jingwei.warehouse.domain.repository.ReceivingLineRepository;
import com.jingwei.warehouse.domain.repository.ReceivingOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 收货单仓库实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class ReceivingOrderRepositoryImpl implements ReceivingOrderRepository {

    private final ReceivingOrderMapper receivingOrderMapper;
    private final ReceivingLineRepository receivingLineRepository;

    @Override
    public ReceivingOrder selectById(Long id) {
        return receivingOrderMapper.selectById(id);
    }

    @Override
    public ReceivingOrder selectDetailById(Long id) {
        ReceivingOrder order = receivingOrderMapper.selectById(id);
        if (order != null) {
            order.setLines(receivingLineRepository.selectByReceivingId(id));
        }
        return order;
    }

    @Override
    public int insert(ReceivingOrder order) {
        return receivingOrderMapper.insert(order);
    }

    @Override
    public int updateById(ReceivingOrder order) {
        return receivingOrderMapper.updateById(order);
    }
}
