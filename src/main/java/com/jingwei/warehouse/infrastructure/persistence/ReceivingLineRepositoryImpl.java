package com.jingwei.warehouse.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.warehouse.domain.model.ReceivingLine;
import com.jingwei.warehouse.domain.repository.ReceivingLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 收货行仓库实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class ReceivingLineRepositoryImpl implements ReceivingLineRepository {

    private final ReceivingLineMapper receivingLineMapper;

    @Override
    public ReceivingLine selectById(Long id) {
        return receivingLineMapper.selectById(id);
    }

    @Override
    public List<ReceivingLine> selectByReceivingId(Long receivingId) {
        return receivingLineMapper.selectList(
                new LambdaQueryWrapper<ReceivingLine>()
                        .eq(ReceivingLine::getReceivingId, receivingId)
                        .orderByAsc(ReceivingLine::getId));
    }

    @Override
    public int batchInsert(List<ReceivingLine> lines) {
        int count = 0;
        for (ReceivingLine line : lines) {
            count += receivingLineMapper.insert(line);
        }
        return count;
    }

    @Override
    public int updateById(ReceivingLine line) {
        return receivingLineMapper.updateById(line);
    }
}
