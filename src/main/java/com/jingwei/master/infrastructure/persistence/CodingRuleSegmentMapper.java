package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.CodingRuleSegment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 编码规则段 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface CodingRuleSegmentMapper extends BaseMapper<CodingRuleSegment> {
}
