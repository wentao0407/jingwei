package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.master.domain.model.CodingRule;
import com.jingwei.master.domain.repository.CodingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 编码规则仓库实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class CodingRuleRepositoryImpl implements CodingRuleRepository {

    private final CodingRuleMapper codingRuleMapper;

    @Override
    public CodingRule selectById(Long id) {
        return codingRuleMapper.selectById(id);
    }

    @Override
    public CodingRule selectByCode(String code) {
        return codingRuleMapper.selectOne(
                new LambdaQueryWrapper<CodingRule>()
                        .eq(CodingRule::getCode, code));
    }

    @Override
    public List<CodingRule> selectAll() {
        return codingRuleMapper.selectList(
                new LambdaQueryWrapper<CodingRule>()
                        .orderByDesc(CodingRule::getCreatedAt));
    }

    @Override
    public int insert(CodingRule rule) {
        return codingRuleMapper.insert(rule);
    }

    @Override
    public int updateById(CodingRule rule) {
        return codingRuleMapper.updateById(rule);
    }

    @Override
    public int deleteById(Long id) {
        return codingRuleMapper.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return codingRuleMapper.selectCount(
                new LambdaQueryWrapper<CodingRule>()
                        .eq(CodingRule::getCode, code)) > 0;
    }
}
