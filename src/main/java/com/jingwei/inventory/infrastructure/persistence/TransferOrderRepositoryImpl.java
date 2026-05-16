package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.inventory.domain.model.TransferOrder;
import com.jingwei.inventory.domain.model.TransferOrderLine;
import com.jingwei.inventory.domain.model.TransferStatus;
import com.jingwei.inventory.domain.repository.TransferOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 调拨单仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TransferOrderRepositoryImpl implements TransferOrderRepository {

    private final TransferOrderMapper transferOrderMapper;
    private final TransferOrderLineMapper transferOrderLineMapper;

    @Override
    public TransferOrder selectById(Long id) {
        return transferOrderMapper.selectById(id);
    }

    @Override
    public TransferOrder selectDetailById(Long id) {
        TransferOrder order = transferOrderMapper.selectById(id);
        if (order != null) {
            List<TransferOrderLine> lines = transferOrderLineMapper.selectList(
                    new LambdaQueryWrapper<TransferOrderLine>()
                            .eq(TransferOrderLine::getTransferId, id));
            order.setLines(lines);
        }
        return order;
    }

    @Override
    public int insert(TransferOrder order) {
        return transferOrderMapper.insert(order);
    }

    @Override
    public int updateById(TransferOrder order) {
        return transferOrderMapper.updateById(order);
    }

    @Override
    public IPage<TransferOrder> selectPage(Page<TransferOrder> page, TransferStatus status) {
        LambdaQueryWrapper<TransferOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(TransferOrder::getStatus, status);
        }
        wrapper.orderByDesc(TransferOrder::getCreatedAt);
        return transferOrderMapper.selectPage(page, wrapper);
    }
}
