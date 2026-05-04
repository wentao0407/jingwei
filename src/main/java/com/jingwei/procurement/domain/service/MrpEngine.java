package com.jingwei.procurement.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.order.domain.model.ProductionOrder;
import com.jingwei.order.domain.model.ProductionOrderLine;
import com.jingwei.order.domain.model.SizeMatrix;
import com.jingwei.order.domain.repository.ProductionOrderLineRepository;
import com.jingwei.order.domain.repository.ProductionOrderRepository;
import com.jingwei.procurement.domain.model.*;
import com.jingwei.procurement.domain.repository.BomRepository;
import com.jingwei.procurement.domain.repository.MrpResultRepository;
import com.jingwei.procurement.domain.repository.MrpSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MRP 计算引擎
 * <p>
 * 核心计算流程：
 * <ol>
 *   <li>需求汇总 — 按生产订单行收集各 SPU+Color-way 的尺码数量</li>
 *   <li>BOM 展开 — 对每个需求项展开 BOM，计算每个物料的用量</li>
 *   <li>同物料合并 — 不同款式/颜色使用的同种物料，需求量合并</li>
 *   <li>库存抵扣 — 查询可用库存和在途数量，计算净需求</li>
 *   <li>采购建议 — 考虑起订量、采购倍数，生成建议采购量</li>
 *   <li>供应商匹配 — 为每个采购建议推荐供应商（预留）</li>
 * </ol>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MrpEngine {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionOrderLineRepository productionOrderLineRepository;
    private final BomRepository bomRepository;
    private final InventoryQueryService inventoryQueryService;
    private final MrpResultRepository mrpResultRepository;
    private final MrpSourceRepository mrpSourceRepository;
    private final CodingRuleDomainService codingRuleDomainService;

    /**
     * 执行 MRP 计算
     *
     * @param productionOrderIds 生产订单ID列表
     * @return MRP 计算结果列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<MrpResult> calculate(List<Long> productionOrderIds) {
        String batchNo = codingRuleDomainService.generateCode("MRP_BATCH", Collections.emptyMap());
        LocalDateTime snapshotTime = LocalDateTime.now();

        log.info("开始MRP计算: batchNo={}, 订单数={}", batchNo, productionOrderIds.size());

        // ========== 第1步：需求汇总 ==========
        List<DemandItem> demandItems = collectDemands(productionOrderIds);
        if (demandItems.isEmpty()) {
            log.warn("无有效需求项，跳过MRP计算");
            return List.of();
        }

        // ========== 第2步+BOM展开 + 第3步：同物料合并 ==========
        Map<Long, MaterialDemand> mergedDemands = expandAndMerge(demandItems);

        // ========== 第4步：库存抵扣 ==========
        List<MrpResult> results = deductInventory(mergedDemands, batchNo, snapshotTime);

        // ========== 第5步：采购建议（MOQ等约束） ==========
        applyPurchaseConstraints(results);

        // ========== 第6步：供应商匹配（预留） ==========
        matchSuppliers(results);

        // ========== 保存结果 ==========
        for (MrpResult result : results) {
            mrpResultRepository.insert(result);
        }

        // 保存来源追溯
        List<MrpSource> allSources = buildSources(demandItems, mergedDemands, batchNo);
        for (MrpSource source : allSources) {
            // 找到对应的 resultId
            results.stream()
                    .filter(r -> r.getMaterialId().equals(source.getMaterialId()))
                    .findFirst()
                    .ifPresent(r -> source.setResultId(r.getId()));
            mrpSourceRepository.insert(source);
        }

        log.info("MRP计算完成: batchNo={}, 结果数={}", batchNo, results.size());
        return results;
    }

    /**
     * 第1步：需求汇总
     */
    private List<DemandItem> collectDemands(List<Long> productionOrderIds) {
        List<DemandItem> demands = new ArrayList<>();

        for (Long orderId : productionOrderIds) {
            ProductionOrder order = productionOrderRepository.selectById(orderId);
            if (order == null) {
                continue;
            }
            List<ProductionOrderLine> lines = productionOrderLineRepository.selectByOrderId(orderId);
            for (ProductionOrderLine line : lines) {
                if (line.getBomId() == null) {
                    log.warn("生产订单行无BOM，跳过: orderId={}, lineId={}", orderId, line.getId());
                    continue;
                }
                if (line.getSizeMatrix() == null || line.getSizeMatrix().getTotalQuantity() == 0) {
                    log.warn("生产订单行无数量，跳过: orderId={}, lineId={}", orderId, line.getId());
                    continue;
                }
                demands.add(new DemandItem(
                        orderId, line.getId(), line.getSpuId(), line.getColorWayId(),
                        line.getBomId(), line.getSizeMatrix()
                ));
            }
        }
        return demands;
    }

    /**
     * 第2步+BOM展开 + 第3步：同物料合并
     */
    private Map<Long, MaterialDemand> expandAndMerge(List<DemandItem> demandItems) {
        Map<Long, MaterialDemand> merged = new LinkedHashMap<>();

        for (DemandItem demand : demandItems) {
            Bom bom = bomRepository.selectDetailById(demand.getBomId());
            if (bom == null || bom.getItems().isEmpty()) {
                log.warn("BOM为空: bomId={}", demand.getBomId());
                continue;
            }

            for (BomItem bomItem : bom.getItems()) {
                BigDecimal quantity = calculateMaterialQuantity(bomItem, demand);

                MaterialDemand existing = merged.get(bomItem.getMaterialId());
                if (existing != null) {
                    existing.addQuantity(quantity);
                } else {
                    merged.put(bomItem.getMaterialId(), new MaterialDemand(
                            bomItem.getMaterialId(), bomItem.getMaterialType(),
                            bomItem.getUnit(), quantity, bomItem.getWastageRate()
                    ));
                }
            }
        }
        return merged;
    }

    /**
     * 计算单个物料在某个需求项上的用量
     */
    BigDecimal calculateMaterialQuantity(BomItem bomItem, DemandItem demand) {
        switch (bomItem.getConsumptionType()) {
            case FIXED_PER_PIECE: {
                // 每件固定用量 × 总件数
                int totalPieces = demand.getSizeMatrix().getTotalQuantity();
                return bomItem.getBaseConsumption()
                        .multiply(BigDecimal.valueOf(totalPieces))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            case SIZE_DEPENDENT: {
                // 各尺码用量 × 对应件数，求和，再乘(1+损耗率)
                BigDecimal total = BigDecimal.ZERO;
                SizeConsumptions sizeMap = bomItem.getSizeConsumptions();
                if (sizeMap == null) {
                    throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED,
                            "SIZE_DEPENDENT 类型必须有尺码用量表");
                }

                for (SizeMatrix.SizeEntry entry : demand.getSizeMatrix().getSizes()) {
                    BigDecimal consumption = sizeMap.getConsumption(entry.getSizeId());
                    if (consumption == null) {
                        throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED,
                                "尺码 " + entry.getCode() + " 未填写用量");
                    }
                    total = total.add(consumption.multiply(BigDecimal.valueOf(entry.getQuantity())));
                }

                // 加损耗
                BigDecimal wastageRate = bomItem.getWastageRate() != null ? bomItem.getWastageRate() : BigDecimal.ZERO;
                BigDecimal wastageMultiplier = BigDecimal.ONE.add(wastageRate);
                return total.multiply(wastageMultiplier).setScale(2, RoundingMode.HALF_UP);
            }

            case PER_ORDER: {
                // 按订单整体用量
                return bomItem.getBaseConsumption();
            }

            default:
                throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED,
                        "未知的消耗类型: " + bomItem.getConsumptionType());
        }
    }

    /**
     * 第4步：库存抵扣
     */
    private List<MrpResult> deductInventory(Map<Long, MaterialDemand> mergedDemands,
                                             String batchNo, LocalDateTime snapshotTime) {
        List<MrpResult> results = new ArrayList<>();

        for (MaterialDemand demand : mergedDemands.values()) {
            BigDecimal availableStock = inventoryQueryService.getAvailableStock(demand.getMaterialId());
            BigDecimal inTransit = inventoryQueryService.getInTransitQuantity(demand.getMaterialId());

            BigDecimal netDemand = demand.getQuantity()
                    .subtract(availableStock)
                    .subtract(inTransit);
            if (netDemand.compareTo(BigDecimal.ZERO) < 0) {
                netDemand = BigDecimal.ZERO;
            }

            MrpResult result = new MrpResult();
            result.setBatchNo(batchNo);
            result.setMaterialId(demand.getMaterialId());
            result.setMaterialType(demand.getMaterialType());
            result.setGrossDemand(demand.getQuantity());
            result.setAllocatedStock(availableStock);
            result.setInTransitQuantity(inTransit);
            result.setNetDemand(netDemand);
            result.setSuggestedQuantity(netDemand); // 初始等于净需求，后续应用约束
            result.setUnit(demand.getUnit());
            result.setStatus(MrpResultStatus.PENDING);
            result.setSnapshotTime(snapshotTime);

            results.add(result);
        }
        return results;
    }

    /**
     * 第5步：采购建议约束（预留 — 需要供应商MOQ数据）
     */
    private void applyPurchaseConstraints(List<MrpResult> results) {
        // 预留：当有供应商主数据后，可在此处应用 MOQ 和采购倍数约束
        // 当前直接使用净需求作为建议采购量
        log.debug("[预留] 采购约束应用（MOQ/采购倍数）— 当前跳过");
    }

    /**
     * 第6步：供应商匹配（预留）
     */
    private void matchSuppliers(List<MrpResult> results) {
        // 预留：当有供应商-物料关系数据后，可在此处匹配最优供应商
        log.debug("[预留] 供应商匹配 — 当前跳过");
    }

    /**
     * 构建来源追溯数据
     */
    private List<MrpSource> buildSources(List<DemandItem> demandItems,
                                          Map<Long, MaterialDemand> mergedDemands,
                                          String batchNo) {
        List<MrpSource> sources = new ArrayList<>();
        for (DemandItem demand : demandItems) {
            Bom bom = bomRepository.selectDetailById(demand.getBomId());
            if (bom == null) continue;

            for (BomItem bomItem : bom.getItems()) {
                BigDecimal quantity = calculateMaterialQuantity(bomItem, demand);

                MrpSource source = new MrpSource();
                source.setBatchNo(batchNo);
                source.setProductionOrderId(demand.getProductionOrderId());
                source.setProductionLineId(demand.getProductionLineId());
                source.setSpuId(demand.getSpuId());
                source.setColorWayId(demand.getColorWayId());
                source.setMaterialId(bomItem.getMaterialId());
                source.setBomId(demand.getBomId());
                source.setDemandQuantity(quantity);

                sources.add(source);
            }
        }
        return sources;
    }

    // ==================== 内部数据结构 ====================

    /**
     * 需求项 — 一个生产订单行的需求
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    static class DemandItem {
        private final Long productionOrderId;
        private final Long productionLineId;
        private final Long spuId;
        private final Long colorWayId;
        private final Long bomId;
        private final SizeMatrix sizeMatrix;
    }

    /**
     * 物料需求 — 合并后的单一物料需求
     */
    @lombok.Getter
    static class MaterialDemand {
        private final Long materialId;
        private final String materialType;
        private final String unit;
        private BigDecimal quantity;
        private final BigDecimal wastageRate;

        MaterialDemand(Long materialId, String materialType, String unit,
                       BigDecimal quantity, BigDecimal wastageRate) {
            this.materialId = materialId;
            this.materialType = materialType;
            this.unit = unit;
            this.quantity = quantity;
            this.wastageRate = wastageRate;
        }

        void addQuantity(BigDecimal additional) {
            this.quantity = this.quantity.add(additional);
        }
    }
}
