package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.inventory.domain.model.InboundOrderLine;
import com.jingwei.inventory.domain.repository.InboundOrderLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 入库单行仓库实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class InboundOrderLineRepositoryImpl implements InboundOrderLineRepository {

    private final InboundOrderLineMapper inboundOrderLineMapper;

    @Override
    public List<InboundOrderLine> selectByInboundId(Long inboundId) {
        return inboundOrderLineMapper.selectList(
                new LambdaQueryWrapper<InboundOrderLine>()
                        .eq(InboundOrderLine::getInboundId, inboundId)
                        .orderByAsc(InboundOrderLine::getLineNo));
    }

    @Override
    public int batchInsert(List<InboundOrderLine> lines) {
        int count = 0;
        for (InboundOrderLine line : lines) {
            count += inboundOrderLineMapper.insert(line);
        }
        return count;
    }

    @Override
    public int deleteByInboundId(Long inboundId) {
        return inboundOrderLineMapper.delete(
                new LambdaQueryWrapper<InboundOrderLine>()
                        .eq(InboundOrderLine::getInboundId, inboundId));
    }
}
