package com.jingwei.inventory.domain.repository;

import com.jingwei.inventory.domain.model.AlertRule;

import java.util.List;

/**
 * 预警规则仓库接口
 *
 * @author JingWei
 */
public interface AlertRuleRepository {

    AlertRule selectById(Long id);

    /** 查询所有已启用的规则 */
    List<AlertRule> selectAllEnabled();

    int insert(AlertRule rule);

    int updateById(AlertRule rule);

    int deleteById(Long id);
}
