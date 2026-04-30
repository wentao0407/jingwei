package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.master.domain.model.CodingRuleSegment;
import com.jingwei.master.domain.repository.CodingRuleSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 编码规则段仓库实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class CodingRuleSegmentRepositoryImpl implements CodingRuleSegmentRepository {

    private final CodingRuleSegmentMapper codingRuleSegmentMapper;

    @Override
    public List<CodingRuleSegment> selectByRuleId(Long ruleId) {
        return codingRuleSegmentMapper.selectList(
                new LambdaQueryWrapper<CodingRuleSegment>()
                        .eq(CodingRuleSegment::getRuleId, ruleId)
                        .orderByAsc(CodingRuleSegment::getSortOrder));
    }

    @Override
    public int batchInsert(List<CodingRuleSegment> segments) {
        int count = 0;
        for (CodingRuleSegment segment : segments) {
            count += codingRuleSegmentMapper.insert(segment);
        }
        return count;
    }

    @Override
    public int deleteByRuleId(Long ruleId) {
        return codingRuleSegmentMapper.delete(
                new LambdaQueryWrapper<CodingRuleSegment>()
                        .eq(CodingRuleSegment::getRuleId, ruleId));
    }
}
