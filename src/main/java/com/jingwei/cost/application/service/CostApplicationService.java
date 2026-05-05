package com.jingwei.cost.application.service;

import com.jingwei.cost.domain.model.CostMaterialIssue;
import com.jingwei.cost.domain.model.CostProductionOrder;
import com.jingwei.cost.domain.model.MaterialType;
import com.jingwei.cost.domain.repository.CostMaterialIssueRepository;
import com.jingwei.cost.domain.repository.CostProductionOrderRepository;
import com.jingwei.cost.domain.service.CostDomainService;
import com.jingwei.cost.interfaces.vo.CostMaterialIssueVO;
import com.jingwei.cost.interfaces.vo.CostProductionOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成本核算应用服务 — 编排层
 * <p>
 * 负责成本数据的查询和展示。
 * 成本记录由领料出库和生产入库自动触发，不对外暴露写入接口。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CostApplicationService {

    private final CostDomainService costDomainService;
    private final CostProductionOrderRepository costProductionOrderRepository;
    private final CostMaterialIssueRepository costMaterialIssueRepository;

    /**
     * 物料类型枚举到中文标签的映射
     * <p>
     * 枚举编码 → 中文标签，用于 VO 层展示。
     * 例如：MATERIAL → "面料"，TRIM → "辅料"，PACKAGING → "包材"
     * </p>
     */
    private static final Map<String, String> MATERIAL_TYPE_LABELS = Arrays.stream(MaterialType.values())
            .collect(Collectors.toMap(MaterialType::name, MaterialType::getLabel));

    /**
     * 查询生产订单成本详情
     *
     * @param productionOrderId 生产订单ID
     * @param productionLineId  生产订单行ID
     * @return 成本详情 VO
     */
    public CostProductionOrderVO getCostDetail(Long productionOrderId, Long productionLineId) {
        CostProductionOrder costRecord = costProductionOrderRepository
                .selectByOrderLineId(productionOrderId, productionLineId);

        if (costRecord == null) {
            return null;
        }

        return toVO(costRecord);
    }

    /**
     * 查询生产订单的领料成本明细
     *
     * @param productionOrderId 生产订单ID
     * @return 领料成本明细列表
     */
    public List<CostMaterialIssueVO> getIssueDetails(Long productionOrderId) {
        List<CostMaterialIssue> issues = costMaterialIssueRepository
                .selectByProductionOrderId(productionOrderId);

        return issues.stream()
                .map(this::toIssueVO)
                .toList();
    }

    // ==================== 私有方法 ====================

    private CostProductionOrderVO toVO(CostProductionOrder cost) {
        CostProductionOrderVO vo = new CostProductionOrderVO();
        vo.setId(cost.getId());
        vo.setProductionOrderId(cost.getProductionOrderId());
        vo.setProductionLineId(cost.getProductionLineId());
        vo.setMaterialCost(cost.getMaterialCost());
        vo.setTrimCost(cost.getTrimCost());
        vo.setPackagingCost(cost.getPackagingCost());
        vo.setTotalCost(cost.getTotalCost());
        vo.setCompletedQty(cost.getCompletedQty());
        vo.setUnitCost(cost.getUnitCost());
        vo.setUpdatedAt(cost.getUpdatedAt());
        return vo;
    }

    private CostMaterialIssueVO toIssueVO(CostMaterialIssue issue) {
        CostMaterialIssueVO vo = new CostMaterialIssueVO();
        vo.setId(issue.getId());
        vo.setProductionOrderId(issue.getProductionOrderId());
        vo.setProductionLineId(issue.getProductionLineId());
        vo.setMaterialId(issue.getMaterialId());
        vo.setMaterialType(issue.getMaterialType().name());
        vo.setMaterialTypeLabel(MATERIAL_TYPE_LABELS.getOrDefault(
                issue.getMaterialType().name(), issue.getMaterialType().name()));
        vo.setIssueQty(issue.getIssueQty());
        vo.setUnitCost(issue.getUnitCost());
        vo.setCostAmount(issue.getCostAmount());
        vo.setIssueDate(issue.getIssueDate());
        vo.setCreatedAt(issue.getCreatedAt());
        return vo;
    }
}
