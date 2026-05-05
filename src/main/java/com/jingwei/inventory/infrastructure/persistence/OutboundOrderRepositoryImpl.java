package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.inventory.domain.model.OutboundOrder;
import com.jingwei.inventory.domain.model.OutboundStatus;
import com.jingwei.inventory.domain.repository.OutboundOrderLineRepository;
import com.jingwei.inventory.domain.repository.OutboundOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 出库单仓库实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class OutboundOrderRepositoryImpl implements OutboundOrderRepository {

    private final OutboundOrderMapper outboundOrderMapper;
    private final OutboundOrderLineRepository outboundOrderLineRepository;

    @Override
    public OutboundOrder selectById(Long id) {
        return outboundOrderMapper.selectById(id);
    }

    @Override
    public OutboundOrder selectDetailById(Long id) {
        OutboundOrder order = outboundOrderMapper.selectById(id);
        if (order != null) {
            order.setLines(outboundOrderLineRepository.selectByOutboundId(id));
        }
        return order;
    }

    @Override
    public IPage<OutboundOrder> selectPage(Page<OutboundOrder> page, OutboundStatus status, Long warehouseId, String outboundNo) {
        LambdaQueryWrapper<OutboundOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(OutboundOrder::getStatus, status);
        if (warehouseId != null) wrapper.eq(OutboundOrder::getWarehouseId, warehouseId);
        if (outboundNo != null && !outboundNo.isBlank()) wrapper.like(OutboundOrder::getOutboundNo, outboundNo);
        wrapper.orderByDesc(OutboundOrder::getCreatedAt);
        return outboundOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public int insert(OutboundOrder order) {
        return outboundOrderMapper.insert(order);
    }

    @Override
    public int updateById(OutboundOrder order) {
        return outboundOrderMapper.updateById(order);
    }

    @Override
    public int deleteById(Long id) {
        return outboundOrderMapper.deleteById(id);
    }
}
