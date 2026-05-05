package com.jingwei.order.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.order.domain.model.OrderChangeLog;
import com.jingwei.order.domain.model.ProductionOrder;
import com.jingwei.order.domain.model.ProductionOrderLine;
import com.jingwei.order.domain.model.ProductionOrderSource;
import com.jingwei.order.domain.model.ProductionOrderSourceType;
import com.jingwei.order.domain.model.ProductionOrderStatus;
import com.jingwei.order.domain.model.SalesOrder;
import com.jingwei.order.domain.model.SalesOrderLine;
import com.jingwei.order.domain.model.SalesOrderStatus;
import com.jingwei.order.domain.model.SizeMatrix;
import com.jingwei.order.domain.repository.OrderChangeLogRepository;
import com.jingwei.order.domain.repository.ProductionOrderLineRepository;
import com.jingwei.order.domain.repository.ProductionOrderRepository;
import com.jingwei.order.domain.repository.ProductionOrderSourceRepository;
import com.jingwei.order.domain.repository.SalesOrderLineRepository;
import com.jingwei.order.domain.repository.SalesOrderRepository;
import com.jingwei.procurement.domain.model.Bom;
import com.jingwei.procurement.domain.repository.BomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单转化领域服务
 * <p>
 * 负责销售订单→生产订单的转化核心逻辑。
 * 不涉及事务管理（由 ApplicationService 负责），也不涉及编号生成（由 ApplicationService 负责）。
 * </p>
 * <p>
 * 核心业务规则：
 * <ul>
 *   <li>只有 CONFIRMED 状态的销售订单允许转化</li>
 *   <li>选中的行必须属于该销售订单</li>
 *   <li>同一行不可重复全额转化（已分配数量 = 行总数量时视为已全额转化）</li>
 *   <li>生成生产订单时自动关联 BOM（从 SPU 的 defaultBomId 获取已审批 BOM）</li>
 *   <li>同一 spuId 的多行合并为一个生产订单行（同款合并）</li>
 *   <li>建立 order_production_source 多对多关联记录</li>
 *   <li>转化后通过状态机将销售订单推进到 PRODUCING</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConvertDomainService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionOrderLineRepository productionOrderLineRepository;
    private final ProductionOrderSourceRepository productionOrderSourceRepository;
    private final OrderChangeLogRepository orderChangeLogRepository;
    private final BomRepository bomRepository;

    /**
     * 执行订单转化
     * <p>
     * 流程：
     * <ol>
     *   <li>校验销售订单状态为 CONFIRMED</li>
     *   <li>校验选中的行属于该订单</li>
     *   <li>校验行未全额转化</li>
     *   <li>按 spuId 分组，查找已审批 BOM</li>
     *   <li>合并同 spuId+colorWayId 的行，创建生产订单行</li>
     *   <li>创建生产订单（sourceType=SALES_ORDER）</li>
     *   <li>保存生产订单行</li>
     *   <li>建立 order_production_source 关联</li>
     *   <li>更新销售订单状态为 PRODUCING</li>
     *   <li>记录变更日志</li>
     * </ol>
     * </p>
     *
     * @param salesOrderId 销售订单ID
     * @param lineIds      选中的销售订单行ID列表
     * @param skipCuttingMap  每行的 skipCutting 配置（key=salesOrderLineId）
     * @param workshopId   车间ID（可选）
     * @param operatorId   操作人ID
     * @return 生成的生产订单列表
     */
    public List<ProductionOrder> convertToProduction(Long salesOrderId, List<Long> lineIds,
                                                      Map<Long, Boolean> skipCuttingMap,
                                                      Long operatorId) {
        // 1. 校验销售订单存在且状态为 CONFIRMED
        SalesOrder salesOrder = salesOrderRepository.selectById(salesOrderId);
        if (salesOrder == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "销售订单不存在");
        }
        if (salesOrder.getStatus() != SalesOrderStatus.CONFIRMED) {
            throw new BizException(ErrorCode.ORDER_NOT_CONFIRMED);
        }

        // 2. 查询并校验选中的行
        List<SalesOrderLine> selectedLines = validateAndLoadLines(salesOrderId, lineIds);

        // 3. 校验每行未全额转化
        validateNotFullyConverted(selectedLines);

        // 4. 按 spuId 分组
        Map<Long, List<SalesOrderLine>> spuGroupMap = groupBySpuId(selectedLines);

        // 5. 为每个 spuId 分组创建生产订单
        List<ProductionOrder> createdOrders = new ArrayList<>();
        List<ProductionOrderSource> allSources = new ArrayList<>();

        for (Map.Entry<Long, List<SalesOrderLine>> entry : spuGroupMap.entrySet()) {
            Long spuId = entry.getKey();
            List<SalesOrderLine> spuLines = entry.getValue();

            // 5a. 查找已审批 BOM
            Bom bom = bomRepository.selectApprovedBySpuId(spuId)
                    .orElseThrow(() -> new BizException(ErrorCode.ORDER_SPU_NO_BOM,
                            "款式ID=" + spuId + " 未配置已审批的BOM"));

            // 5b. 按 spuId+colorWayId 合并行
            Map<String, MergedLine> mergedMap = mergeLinesByColor(spuLines, skipCuttingMap);

            // 5c. 创建生产订单行
            List<ProductionOrderLine> prodLines = new ArrayList<>();
            for (MergedLine merged : mergedMap.values()) {
                ProductionOrderLine prodLine = new ProductionOrderLine();
                prodLine.setSpuId(spuId);
                prodLine.setColorWayId(merged.colorWayId);
                prodLine.setBomId(bom.getId());
                prodLine.setSizeMatrix(merged.mergedMatrix);
                prodLine.setTotalQuantity(merged.mergedMatrix.getTotalQuantity());
                prodLine.setSkipCutting(merged.skipCutting);
                prodLine.setStatus(ProductionOrderStatus.DRAFT);
                prodLine.setCompletedQuantity(0);
                prodLine.setStockedQuantity(0);
                prodLines.add(prodLine);
            }

            // 5d. 创建生产订单（编号由 ApplicationService 生成后设置）
            // 这里先不设置 orderNo，由 ApplicationService 负责
            ProductionOrder prodOrder = new ProductionOrder();
            prodOrder.setSourceType(ProductionOrderSourceType.SALES_ORDER.getCode());
            prodOrder.setStatus(ProductionOrderStatus.DRAFT);
            prodOrder.setCompletedQuantity(0);
            prodOrder.setStockedQuantity(0);

            // 计算总数量
            int totalQty = prodLines.stream().mapToInt(ProductionOrderLine::getTotalQuantity).sum();
            prodOrder.setTotalQuantity(totalQty);

            createdOrders.add(prodOrder);

            // 5e. 构建 source 关联记录（需要在持久化后设置ID）
            // 暂存，持久化后补全
            for (int i = 0; i < prodLines.size(); i++) {
                ProductionOrderLine prodLine = prodLines.get(i);
                // 找到对应的源销售订单行
                MergedLine merged = mergedMap.values().stream().toList().get(i);
                for (SourceRef ref : merged.sourceRefs) {
                    ProductionOrderSource source = new ProductionOrderSource();
                    source.setSalesOrderId(salesOrderId);
                    source.setSalesLineId(ref.salesLineId);
                    source.setAllocatedQuantity(ref.allocatedQuantity);
                    allSources.add(source);
                    // 暂存 prodLine 引用，持久化后补全 prodOrderId 和 prodLineId
                    source.setProductionOrderId(-1L); // 占位
                    source.setProductionLineId(-1L); // 占位
                }
            }

            // 将 prodLines 和 sources 关联到 prodOrder（供 ApplicationService 持久化）
            prodOrder.setLines(prodLines);
        }

        // 6. 更新销售订单状态为 PRODUCING
        salesOrder.setStatus(SalesOrderStatus.PRODUCING);
        salesOrderRepository.updateById(salesOrder);

        // 7. 记录变更日志
        logConversion(salesOrderId, lineIds, operatorId);

        log.info("订单转化完成: salesOrderId={}, 生成生产订单数={}, 操作人={}",
                salesOrderId, createdOrders.size(), operatorId);

        return createdOrders;
    }

    /**
     * 查询销售订单行的已分配总量
     *
     * @param salesLineId 销售订单行ID
     * @return 已分配数量
     */
    public int getAllocatedQuantity(Long salesLineId) {
        return productionOrderSourceRepository.selectBySalesLineId(salesLineId).stream()
                .mapToInt(ProductionOrderSource::getAllocatedQuantity)
                .sum();
    }

    // ==================== 私有方法 ====================

    /**
     * 校验并加载选中的销售订单行
     * <p>
     * 确保每行都属于指定的销售订单。
     * </p>
     */
    private List<SalesOrderLine> validateAndLoadLines(Long salesOrderId, List<Long> lineIds) {
        List<SalesOrderLine> allLines = salesOrderLineRepository.selectByOrderId(salesOrderId);
        Map<Long, SalesOrderLine> lineMap = new LinkedHashMap<>();
        for (SalesOrderLine line : allLines) {
            lineMap.put(line.getId(), line);
        }

        List<SalesOrderLine> selectedLines = new ArrayList<>();
        for (Long lineId : lineIds) {
            SalesOrderLine line = lineMap.get(lineId);
            if (line == null) {
                throw new BizException(ErrorCode.ORDER_LINE_NOT_BELONG,
                        "订单行ID=" + lineId + " 不属于销售订单ID=" + salesOrderId);
            }
            selectedLines.add(line);
        }
        return selectedLines;
    }

    /**
     * 校验选中的行未全额转化
     * <p>
     * 已分配数量 >= 行总数量时，视为已全额转化，不允许重复转化。
     * </p>
     */
    private void validateNotFullyConverted(List<SalesOrderLine> lines) {
        for (SalesOrderLine line : lines) {
            int allocated = getAllocatedQuantity(line.getId());
            if (allocated >= line.getTotalQuantity()) {
                throw new BizException(ErrorCode.ORDER_ALREADY_CONVERTED,
                        "订单行ID=" + line.getId() + " 已全部转化（已分配" + allocated + "件）");
            }
        }
    }

    /**
     * 按 spuId 分组
     */
    private Map<Long, List<SalesOrderLine>> groupBySpuId(List<SalesOrderLine> lines) {
        Map<Long, List<SalesOrderLine>> map = new LinkedHashMap<>();
        for (SalesOrderLine line : lines) {
            map.computeIfAbsent(line.getSpuId(), k -> new ArrayList<>()).add(line);
        }
        return map;
    }

    /**
     * 按 spuId+colorWayId 合并行
     * <p>
     * 同款同色的多行合并为一个生产订单行，尺码矩阵按尺码维度累加。
     * </p>
     *
     * @param lines         同 spuId 的销售订单行
     * @param skipCuttingMap skipCutting 配置
     * @return 合并后的行（key=colorWayId）
     */
    private Map<String, MergedLine> mergeLinesByColor(List<SalesOrderLine> lines,
                                                       Map<Long, Boolean> skipCuttingMap) {
        Map<String, MergedLine> map = new LinkedHashMap<>();
        for (SalesOrderLine line : lines) {
            String key = line.getSpuId() + "_" + line.getColorWayId();
            MergedLine merged = map.get(key);
            if (merged == null) {
                merged = new MergedLine();
                merged.colorWayId = line.getColorWayId();
                merged.skipCutting = skipCuttingMap.getOrDefault(line.getId(), false);
                merged.sourceRefs = new ArrayList<>();
                // 初始化合并矩阵：以第一行的矩阵为基础
                merged.mergedMatrix = copyMatrix(line.getSizeMatrix());
                map.put(key, merged);
            } else {
                // 累加尺码矩阵
                merged.mergedMatrix = mergeMatrix(merged.mergedMatrix, line.getSizeMatrix());
            }
            // 记录来源：未转化的部分 = 行总数量 - 已分配数量
            int allocated = getAllocatedQuantity(line.getId());
            int convertible = line.getTotalQuantity() - allocated;
            SourceRef ref = new SourceRef();
            ref.salesLineId = line.getId();
            ref.allocatedQuantity = convertible;
            merged.sourceRefs.add(ref);
        }
        return map;
    }

    /**
     * 复制尺码矩阵（深拷贝）
     */
    private SizeMatrix copyMatrix(SizeMatrix source) {
        if (source == null) {
            return null;
        }
        List<SizeMatrix.SizeEntry> copiedSizes = source.getSizes().stream()
                .map(s -> new SizeMatrix.SizeEntry(s.getSizeId(), s.getCode(), s.getQuantity()))
                .toList();
        return new SizeMatrix(source.getSizeGroupId(), copiedSizes);
    }

    /**
     * 合并两个尺码矩阵（按 sizeId 累加数量）
     */
    private SizeMatrix mergeMatrix(SizeMatrix base, SizeMatrix addition) {
        if (base == null || addition == null) {
            return base != null ? base : addition;
        }
        // 将 addition 的数量按 sizeId 累加到 base
        Map<Long, Integer> additionMap = new LinkedHashMap<>();
        for (SizeMatrix.SizeEntry entry : addition.getSizes()) {
            additionMap.merge(entry.getSizeId(), entry.getQuantity(), Integer::sum);
        }

        List<SizeMatrix.SizeEntry> mergedSizes = base.getSizes().stream()
                .map(s -> {
                    int extra = additionMap.getOrDefault(s.getSizeId(), 0);
                    return new SizeMatrix.SizeEntry(s.getSizeId(), s.getCode(), s.getQuantity() + extra);
                })
                .toList();

        return new SizeMatrix(base.getSizeGroupId(), mergedSizes);
    }

    /**
     * 记录转化变更日志
     */
    private void logConversion(Long salesOrderId, List<Long> lineIds, Long operatorId) {
        OrderChangeLog changeLog = new OrderChangeLog();
        changeLog.setOrderType("SALES");
        changeLog.setOrderId(salesOrderId);
        changeLog.setChangeType("STATUS_CHANGE");
        changeLog.setFieldName("status");
        changeLog.setOldValue(SalesOrderStatus.CONFIRMED.name());
        changeLog.setNewValue(SalesOrderStatus.PRODUCING.name());
        changeLog.setChangeReason("订单转化：选中" + lineIds.size() + "行生成生产订单");
        changeLog.setOperatedBy(operatorId);
        changeLog.setOperatedAt(LocalDateTime.now());
        orderChangeLogRepository.insert(changeLog);
    }

    /**
     * 内部值对象：合并后的行
     */
    private static class MergedLine {
        Long colorWayId;
        SizeMatrix mergedMatrix;
        Boolean skipCutting;
        List<SourceRef> sourceRefs;
    }

    /**
     * 内部值对象：来源引用
     */
    private static class SourceRef {
        Long salesLineId;
        Integer allocatedQuantity;
    }
}
