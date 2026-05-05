package com.jingwei.cost.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.cost.domain.model.CostMaterialIssue;
import com.jingwei.cost.domain.repository.CostMaterialIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 领料成本记录仓储实现
 *
 * @author JingWei
 */
@Repository
@RequiredArgsConstructor
public class CostMaterialIssueRepositoryImpl implements CostMaterialIssueRepository {

    private final CostMaterialIssueMapper costMaterialIssueMapper;

    @Override
    public void insert(CostMaterialIssue issue) {
        costMaterialIssueMapper.insert(issue);
    }

    @Override
    public List<CostMaterialIssue> selectByProductionOrderId(Long productionOrderId) {
        LambdaQueryWrapper<CostMaterialIssue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CostMaterialIssue::getProductionOrderId, productionOrderId);
        wrapper.orderByAsc(CostMaterialIssue::getCreatedAt);
        return costMaterialIssueMapper.selectList(wrapper);
    }

    @Override
    public BigDecimal sumCostByOrderLineId(Long productionOrderId, Long productionLineId) {
        return costMaterialIssueMapper.sumCostByOrderLineId(productionOrderId, productionLineId);
    }
}
