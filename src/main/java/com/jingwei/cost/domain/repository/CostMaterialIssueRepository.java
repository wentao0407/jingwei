package com.jingwei.cost.domain.repository;

import com.jingwei.cost.domain.model.CostMaterialIssue;

import java.math.BigDecimal;
import java.util.List;

/**
 * 领料成本记录仓储接口
 *
 * @author JingWei
 */
public interface CostMaterialIssueRepository {

    /**
     * 插入领料成本记录
     *
     * @param issue 领料成本记录
     */
    void insert(CostMaterialIssue issue);

    /**
     * 根据生产订单ID查询所有领料记录
     *
     * @param productionOrderId 生产订单ID
     * @return 领料成本记录列表
     */
    List<CostMaterialIssue> selectByProductionOrderId(Long productionOrderId);

    /**
     * 根据生产订单行ID查询领料成本合计
     *
     * @param productionOrderId 生产订单ID
     * @param productionLineId  生产订单行ID
     * @return 领料成本合计
     */
    BigDecimal sumCostByOrderLineId(Long productionOrderId, Long productionLineId);
}
