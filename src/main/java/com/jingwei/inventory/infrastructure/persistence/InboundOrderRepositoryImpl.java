package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.inventory.domain.model.InboundOrder;
import com.jingwei.inventory.domain.model.InboundOrderLine;
import com.jingwei.inventory.domain.model.InboundStatus;
import com.jingwei.inventory.domain.repository.InboundOrderLineRepository;
import com.jingwei.inventory.domain.repository.InboundOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 入库单仓库实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class InboundOrderRepositoryImpl implements InboundOrderRepository {

    private final InboundOrderMapper inboundOrderMapper;
    private final InboundOrderLineRepository inboundOrderLineRepository;

    @Override
    public InboundOrder selectById(Long id) {
        return inboundOrderMapper.selectById(id);
    }

    @Override
    public InboundOrder selectDetailById(Long id) {
        InboundOrder order = inboundOrderMapper.selectById(id);
        if (order != null) {
            order.setLines(inboundOrderLineRepository.selectByInboundId(id));
        }
        return order;
    }

    @Override
    public IPage<InboundOrder> selectPage(Page<InboundOrder> page, InboundStatus status, Long warehouseId, String inboundNo) {
        LambdaQueryWrapper<InboundOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(InboundOrder::getStatus, status);
        if (warehouseId != null) wrapper.eq(InboundOrder::getWarehouseId, warehouseId);
        if (inboundNo != null && !inboundNo.isBlank()) wrapper.like(InboundOrder::getInboundNo, inboundNo);
        wrapper.orderByDesc(InboundOrder::getCreatedAt);
        return inboundOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public int insert(InboundOrder order) {
        return inboundOrderMapper.insert(order);
    }

    @Override
    public int updateById(InboundOrder order) {
        return inboundOrderMapper.updateById(order);
    }

    @Override
    public int deleteById(Long id) {
        return inboundOrderMapper.deleteById(id);
    }
}
