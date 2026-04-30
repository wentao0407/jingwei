package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.master.domain.model.CodingRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 编码规则 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface CodingRuleMapper extends BaseMapper<CodingRule> {
}
