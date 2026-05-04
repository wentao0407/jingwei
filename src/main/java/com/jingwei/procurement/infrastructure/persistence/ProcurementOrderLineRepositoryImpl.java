package com.jingwei.procurement.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.procurement.domain.model.ProcurementOrderLine;
import com.jingwei.procurement.domain.repository.ProcurementOrderLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 采购订单行仓库实现
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProcurementOrderLineRepositoryImpl implements ProcurementOrderLineRepository {

    private final ProcurementOrderLineMapper procurementOrderLineMapper;

    @Override
    public ProcurementOrderLine selectById(Long id) {
        return procurementOrderLineMapper.selectById(id);
    }

    @Override
    public List<ProcurementOrderLine> selectByOrderId(Long orderId) {
        return procurementOrderLineMapper.selectList(
                new LambdaQueryWrapper<ProcurementOrderLine>()
                        .eq(ProcurementOrderLine::getOrderId, orderId)
                        .orderByAsc(ProcurementOrderLine::getLineNo));
    }

    @Override
    public int insert(ProcurementOrderLine line) {
        return procurementOrderLineMapper.insert(line);
    }

    @Override
    public int updateById(ProcurementOrderLine line) {
        return procurementOrderLineMapper.updateById(line);
    }

    @Override
    public int deleteByOrderId(Long orderId) {
        return procurementOrderLineMapper.delete(
                new LambdaQueryWrapper<ProcurementOrderLine>()
                        .eq(ProcurementOrderLine::getOrderId, orderId));
    }
}
