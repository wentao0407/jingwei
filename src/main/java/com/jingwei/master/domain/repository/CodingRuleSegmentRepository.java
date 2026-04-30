package com.jingwei.master.domain.repository;

import com.jingwei.master.domain.model.CodingRuleSegment;

import java.util.List;

/**
 * 编码规则段仓库接口
 *
 * @author JingWei
 */
public interface CodingRuleSegmentRepository {

    List<CodingRuleSegment> selectByRuleId(Long ruleId);

    int batchInsert(List<CodingRuleSegment> segments);

    int deleteByRuleId(Long ruleId);
}
