package com.jingwei.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.order.domain.model.ReturnOrderLine;
import com.jingwei.order.domain.repository.ReturnOrderLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 退货单行仓储实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class ReturnOrderLineRepositoryImpl implements ReturnOrderLineRepository {

    private final ReturnOrderLineMapper returnOrderLineMapper;

    @Override
    public void insertBatch(List<ReturnOrderLine> lines) {
        for (ReturnOrderLine line : lines) {
            returnOrderLineMapper.insert(line);
        }
    }

    @Override
    public List<ReturnOrderLine> selectByReturnId(Long returnId) {
        LambdaQueryWrapper<ReturnOrderLine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReturnOrderLine::getReturnId, returnId);
        wrapper.orderByAsc(ReturnOrderLine::getId);
        return returnOrderLineMapper.selectList(wrapper);
    }

    @Override
    public int updateById(ReturnOrderLine line) {
        return returnOrderLineMapper.updateById(line);
    }

    @Override
    public int sumReturnQtyBySalesOrderLineId(Long salesOrderLineId) {
        return returnOrderLineMapper.sumReturnQtyBySalesOrderLineId(salesOrderLineId);
    }
}
