package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.inventory.domain.model.AlertRule;
import com.jingwei.inventory.domain.repository.AlertRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 预警规则仓库实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class AlertRuleRepositoryImpl implements AlertRuleRepository {

    private final AlertRuleMapper alertRuleMapper;

    @Override
    public AlertRule selectById(Long id) {
        return alertRuleMapper.selectById(id);
    }

    @Override
    public List<AlertRule> selectAllEnabled() {
        return alertRuleMapper.selectList(
                new LambdaQueryWrapper<AlertRule>()
                        .eq(AlertRule::getEnabled, true));
    }

    @Override
    public int insert(AlertRule rule) {
        return alertRuleMapper.insert(rule);
    }

    @Override
    public int updateById(AlertRule rule) {
        return alertRuleMapper.updateById(rule);
    }

    @Override
    public int deleteById(Long id) {
        return alertRuleMapper.deleteById(id);
    }
}
