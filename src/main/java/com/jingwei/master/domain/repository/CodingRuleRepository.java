package com.jingwei.master.domain.repository;

import com.jingwei.master.domain.model.CodingRule;

import java.util.List;

/**
 * 编码规则仓库接口
 *
 * @author JingWei
 */
public interface CodingRuleRepository {

    CodingRule selectById(Long id);

    CodingRule selectByCode(String code);

    List<CodingRule> selectAll();

    int insert(CodingRule rule);

    int updateById(CodingRule rule);

    int deleteById(Long id);

    boolean existsByCode(String code);
}
