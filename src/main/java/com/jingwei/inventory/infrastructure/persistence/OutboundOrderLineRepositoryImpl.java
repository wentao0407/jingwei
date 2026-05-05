package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.inventory.domain.model.OutboundOrderLine;
import com.jingwei.inventory.domain.repository.OutboundOrderLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 出库单行仓库实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class OutboundOrderLineRepositoryImpl implements OutboundOrderLineRepository {

    private final OutboundOrderLineMapper outboundOrderLineMapper;

    @Override
    public List<OutboundOrderLine> selectByOutboundId(Long outboundId) {
        return outboundOrderLineMapper.selectList(
                new LambdaQueryWrapper<OutboundOrderLine>()
                        .eq(OutboundOrderLine::getOutboundId, outboundId)
                        .orderByAsc(OutboundOrderLine::getLineNo));
    }

    @Override
    public int batchInsert(List<OutboundOrderLine> lines) {
        int count = 0;
        for (OutboundOrderLine line : lines) {
            count += outboundOrderLineMapper.insert(line);
        }
        return count;
    }

    @Override
    public int deleteByOutboundId(Long outboundId) {
        return outboundOrderLineMapper.delete(
                new LambdaQueryWrapper<OutboundOrderLine>()
                        .eq(OutboundOrderLine::getOutboundId, outboundId));
    }
}
