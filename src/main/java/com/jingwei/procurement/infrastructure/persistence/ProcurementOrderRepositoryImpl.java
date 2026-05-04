package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.procurement.domain.model.ProcurementOrder;
import com.jingwei.procurement.domain.model.ProcurementOrderLine;
import com.jingwei.procurement.domain.model.ProcurementOrderStatus;
import com.jingwei.procurement.domain.repository.ProcurementOrderLineRepository;
import com.jingwei.procurement.domain.repository.ProcurementOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 采购订单仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProcurementOrderRepositoryImpl implements ProcurementOrderRepository {

    private final ProcurementOrderMapper procurementOrderMapper;
    private final ProcurementOrderLineRepository procurementOrderLineRepository;

    @Override
    public ProcurementOrder selectById(Long id) {
        return procurementOrderMapper.selectById(id);
    }

    @Override
    public ProcurementOrder selectDetailById(Long id) {
        ProcurementOrder order = procurementOrderMapper.selectById(id);
        if (order != null) {
            List<ProcurementOrderLine> lines = procurementOrderLineRepository.selectByOrderId(id);
            order.setLines(lines);
        }
        return order;
    }

    @Override
    public IPage<ProcurementOrder> selectPage(IPage<ProcurementOrder> page,
                                                Long supplierId, ProcurementOrderStatus status) {
        LambdaQueryWrapper<ProcurementOrder> wrapper = new LambdaQueryWrapper<ProcurementOrder>()
                .eq(supplierId != null, ProcurementOrder::getSupplierId, supplierId)
                .eq(status != null, ProcurementOrder::getStatus, status)
                .orderByDesc(ProcurementOrder::getCreatedAt);
        return procurementOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public int insert(ProcurementOrder order) {
        return procurementOrderMapper.insert(order);
    }

    @Override
    public int updateById(ProcurementOrder order) {
        return procurementOrderMapper.updateById(order);
    }
}
