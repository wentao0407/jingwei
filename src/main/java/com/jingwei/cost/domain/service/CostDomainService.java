package com.jingwei.cost.domain.service;

import com.jingwei.cost.domain.model.CostMaterialIssue;
import com.jingwei.cost.domain.model.CostProductionOrder;
import com.jingwei.cost.domain.model.MaterialType;
import com.jingwei.cost.domain.repository.CostMaterialIssueRepository;
import com.jingwei.cost.domain.repository.CostProductionOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * 成本核算领域服务 — 成本流转的核心业务逻辑
 * <p>
 * 负责：
 * <ul>
 *   <li>领料出库时自动记录成本（issue_qty × 领料时 unit_cost）</li>
 *   <li>生产入库时归集总领料成本，计算成品单位成本</li>
 *   <li>移动加权平均法重新计算 unit_cost</li>
 * </ul>
 * </p>
 * <p>
 * 成本流转链路：
 * 采购入库（面料单位成本 = 采购单价）
 * → 领料出库（领料成本 = 面料单位成本 × 领料数量）
 * → 生产入库（成品单位成本 = Σ领料成本 / 完工数量）
 * → 销售出库（销售成本 = 成品单位成本 × 出库数量）
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CostDomainService {

    private final CostProductionOrderRepository costProductionOrderRepository;
    private final CostMaterialIssueRepository costMaterialIssueRepository;

    /**
     * 记录领料成本
     * <p>
     * 领料出库时调用，记录本次领料的成本。
     * 成本金额 = issueQty × unitCost（领料时的物料单位成本）。
     * 同时更新生产订单成本归集表。
     * </p>
     *
     * @param productionOrderId 生产订单ID
     * @param productionLineId  生产订单行ID
     * @param materialId        物料ID
     * @param materialType      物料类型（MATERIAL/TRIM/PACKAGING）
     * @param issueQty          领料数量
     * @param unitCost          领料时物料单位成本
     * @param operationId       关联库存操作流水ID（可选）
     * @return 领料成本记录
     */
    public CostMaterialIssue recordMaterialIssue(Long productionOrderId, Long productionLineId,
                                                  Long materialId, MaterialType materialType,
                                                  BigDecimal issueQty, BigDecimal unitCost,
                                                  Long operationId) {
        // 1. 计算成本金额
        BigDecimal costAmount = issueQty.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);

        // 2. 创建领料成本记录
        CostMaterialIssue issue = new CostMaterialIssue();
        issue.setProductionOrderId(productionOrderId);
        issue.setProductionLineId(productionLineId);
        issue.setMaterialId(materialId);
        issue.setMaterialType(materialType);
        issue.setIssueQty(issueQty);
        issue.setUnitCost(unitCost);
        issue.setCostAmount(costAmount);
        issue.setIssueDate(LocalDate.now());
        issue.setOperationId(operationId);
        costMaterialIssueRepository.insert(issue);

        // 3. 更新生产订单成本归集
        updateProductionOrderCost(productionOrderId, productionLineId, materialType, costAmount);

        log.info("领料成本已记录: productionOrderId={}, materialId={}, qty={}, cost={}",
                productionOrderId, materialId, issueQty, costAmount);

        return issue;
    }

    /**
     * 计算成品单位成本（生产入库时调用）
     * <p>
     * 成品单位成本 = 总领料成本 / 完工数量
     * </p>
     *
     * @param productionOrderId 生产订单ID
     * @param productionLineId  生产订单行ID
     * @param completedQty      完工数量
     * @return 成品单位成本
     */
    public BigDecimal calculateUnitCost(Long productionOrderId, Long productionLineId, int completedQty) {
        if (completedQty <= 0) {
            return BigDecimal.ZERO;
        }

        CostProductionOrder costRecord = costProductionOrderRepository
                .selectByOrderLineId(productionOrderId, productionLineId);

        if (costRecord == null) {
            log.warn("生产订单成本记录不存在: productionOrderId={}, lineId={}",
                    productionOrderId, productionLineId);
            return BigDecimal.ZERO;
        }

        // 更新完工数量和单位成本
        costRecord.setCompletedQty(completedQty);
        BigDecimal unitCost = costRecord.getTotalCost()
                .divide(BigDecimal.valueOf(completedQty), 2, RoundingMode.HALF_UP);
        costRecord.setUnitCost(unitCost);
        costProductionOrderRepository.updateById(costRecord);

        log.info("成品单位成本已计算: productionOrderId={}, completedQty={}, unitCost={}",
                productionOrderId, completedQty, unitCost);

        return unitCost;
    }

    /**
     * 移动加权平均法计算新单位成本
     * <p>
     * 新单位成本 = (原库存金额 + 本次入库金额) / (原库存数量 + 本次入库数量)
     * </p>
     *
     * @param originalQty   原库存数量
     * @param originalCost  原单位成本
     * @param inboundQty    本次入库数量
     * @param inboundCost   本次入库单位成本
     * @return 新的加权平均单位成本
     */
    public BigDecimal calculateWeightedAverageCost(int originalQty, BigDecimal originalCost,
                                                    int inboundQty, BigDecimal inboundCost) {
        if (originalQty + inboundQty <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal originalAmount = originalCost.multiply(BigDecimal.valueOf(originalQty));
        BigDecimal inboundAmount = inboundCost.multiply(BigDecimal.valueOf(inboundQty));
        BigDecimal totalAmount = originalAmount.add(inboundAmount);
        int totalQty = originalQty + inboundQty;

        return totalAmount.divide(BigDecimal.valueOf(totalQty), 2, RoundingMode.HALF_UP);
    }

    /**
     * 查询生产订单成本详情
     *
     * @param productionOrderId 生产订单ID
     * @param productionLineId  生产订单行ID
     * @return 成本归集记录
     */
    public CostProductionOrder getCostDetail(Long productionOrderId, Long productionLineId) {
        return costProductionOrderRepository.selectByOrderLineId(productionOrderId, productionLineId);
    }

    // ==================== 私有方法 ====================

    /**
     * 更新生产订单成本归集
     * <p>
     * 按物料类型累加成本到对应字段
     * </p>
     */
    private void updateProductionOrderCost(Long productionOrderId, Long productionLineId,
                                            MaterialType materialType, BigDecimal costAmount) {
        CostProductionOrder costRecord = costProductionOrderRepository
                .selectByOrderLineId(productionOrderId, productionLineId);

        if (costRecord == null) {
            // 首次记录，创建成本归集
            costRecord = new CostProductionOrder();
            costRecord.setProductionOrderId(productionOrderId);
            costRecord.setProductionLineId(productionLineId);
            costRecord.setMaterialCost(BigDecimal.ZERO);
            costRecord.setTrimCost(BigDecimal.ZERO);
            costRecord.setPackagingCost(BigDecimal.ZERO);
            costRecord.setTotalCost(BigDecimal.ZERO);
            costRecord.setCompletedQty(0);
            costRecord.setUnitCost(BigDecimal.ZERO);

            // 按类型设置成本
            applyCostByType(costRecord, materialType, costAmount);
            costProductionOrderRepository.insert(costRecord);
        } else {
            // 累加成本
            applyCostByType(costRecord, materialType, costAmount);
            costProductionOrderRepository.updateById(costRecord);
        }
    }

    /**
     * 按物料类型累加成本
     */
    private void applyCostByType(CostProductionOrder costRecord, MaterialType materialType, BigDecimal costAmount) {
        switch (materialType) {
            case MATERIAL:
                costRecord.setMaterialCost(costRecord.getMaterialCost().add(costAmount));
                break;
            case TRIM:
                costRecord.setTrimCost(costRecord.getTrimCost().add(costAmount));
                break;
            case PACKAGING:
                costRecord.setPackagingCost(costRecord.getPackagingCost().add(costAmount));
                break;
        }
        // 重新计算总成本
        costRecord.setTotalCost(costRecord.getMaterialCost()
                .add(costRecord.getTrimCost())
                .add(costRecord.getPackagingCost()));
    }
}
