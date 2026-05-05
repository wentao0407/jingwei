package com.jingwei.order.application.service;

import com.jingwei.common.domain.model.UserContext;
import com.jingwei.master.domain.model.ColorWay;
import com.jingwei.master.domain.model.Spu;
import com.jingwei.master.domain.repository.ColorWayRepository;
import com.jingwei.master.domain.repository.SpuRepository;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.order.application.dto.ConvertLineDTO;
import com.jingwei.order.application.dto.ConvertToProductionDTO;
import com.jingwei.order.domain.model.ProductionOrder;
import com.jingwei.order.domain.model.ProductionOrderLine;
import com.jingwei.order.domain.model.ProductionOrderSource;
import com.jingwei.order.domain.model.SalesOrder;
import com.jingwei.order.domain.model.SalesOrderLine;
import com.jingwei.order.domain.repository.ProductionOrderLineRepository;
import com.jingwei.order.domain.repository.ProductionOrderRepository;
import com.jingwei.order.domain.repository.ProductionOrderSourceRepository;
import com.jingwei.order.domain.repository.SalesOrderLineRepository;
import com.jingwei.order.domain.repository.SalesOrderRepository;
import com.jingwei.order.domain.service.OrderConvertDomainService;
import com.jingwei.order.interfaces.vo.ConvertResultVO;
import com.jingwei.order.interfaces.vo.ProductionOrderLineVO;
import com.jingwei.order.interfaces.vo.ProductionOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单转化应用服务
 * <p>
 * 负责销售订单→生产订单转化的编排和事务边界管理。
 * 职责：
 * <ul>
 *   <li>参数组装（DTO → 内部参数）</li>
 *   <li>调用编码规则引擎生成生产订单编号</li>
 *   <li>持久化生产订单及关联记录</li>
 *   <li>事务管理（全部成功或全部回滚）</li>
 *   <li>结果转换（VO）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConvertApplicationService {

    /** 编码规则键：生产订单编号（格式 MO-年月-5位流水号） */
    private static final String PRODUCTION_ORDER_CODE_RULE = "PRODUCTION_ORDER";

    private final OrderConvertDomainService orderConvertDomainService;
    private final CodingRuleDomainService codingRuleDomainService;
    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionOrderLineRepository productionOrderLineRepository;
    private final ProductionOrderSourceRepository productionOrderSourceRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final SpuRepository spuRepository;
    private final ColorWayRepository colorWayRepository;

    /**
     * 从销售订单生成生产订单
     * <p>
     * 编排流程：
     * <ol>
     *   <li>组装参数（DTO → 内部结构）</li>
     *   <li>调用 OrderConvertDomainService 执行转化逻辑（校验 + 合并 + 状态更新）</li>
     *   <li>为每个生成的生产订单生成编号并持久化</li>
     *   <li>持久化生产订单行</li>
     *   <li>建立 order_production_source 关联记录</li>
     *   <li>返回转化结果</li>
     * </ol>
     * </p>
     *
     * @param dto 转化请求
     * @return 转化结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ConvertResultVO convertToProduction(ConvertToProductionDTO dto) {
        Long operatorId = UserContext.getUserId();

        // 1. 组装参数
        List<Long> lineIds = dto.getLines().stream()
                .map(ConvertLineDTO::getSalesOrderLineId)
                .toList();

        Map<Long, Boolean> skipCuttingMap = new HashMap<>();
        for (ConvertLineDTO lineDTO : dto.getLines()) {
            skipCuttingMap.put(lineDTO.getSalesOrderLineId(),
                    lineDTO.getSkipCutting() != null && lineDTO.getSkipCutting());
        }

        // 2. 先查询销售订单行（在 DomainService 修改状态前保存快照）
        List<SalesOrderLine> salesLines = salesOrderLineRepository.selectByOrderId(dto.getSalesOrderId());
        Map<Long, SalesOrderLine> salesLineMap = new HashMap<>();
        for (SalesOrderLine sl : salesLines) {
            salesLineMap.put(sl.getId(), sl);
        }

        // 3. 调用领域服务执行转化（校验 + 合并 + 销售订单状态更新）
        List<ProductionOrder> createdOrders = orderConvertDomainService.convertToProduction(
                dto.getSalesOrderId(), lineIds, skipCuttingMap, operatorId);

        // 4. 持久化每个生产订单
        List<ProductionOrderVO> orderVOs = new ArrayList<>();
        for (ProductionOrder prodOrder : createdOrders) {
            // 生成生产订单编号
            String orderNo = codingRuleDomainService.generateCode(
                    PRODUCTION_ORDER_CODE_RULE, java.util.Collections.emptyMap());
            prodOrder.setOrderNo(orderNo);

            // 设置可选字段
            if (dto.getWorkshopId() != null) {
                prodOrder.setWorkshopId(dto.getWorkshopId());
            }
            if (dto.getDeadlineDate() != null) {
                prodOrder.setDeadlineDate(LocalDate.parse(dto.getDeadlineDate()));
            }
            if (dto.getRemark() != null) {
                prodOrder.setRemark(dto.getRemark());
            }

            // 持久化主表
            productionOrderRepository.insert(prodOrder);

            // 持久化行
            List<ProductionOrderLine> prodLines = prodOrder.getLines();
            for (int i = 0; i < prodLines.size(); i++) {
                prodLines.get(i).setOrderId(prodOrder.getId());
                prodLines.get(i).setLineNo(i + 1);
            }
            productionOrderLineRepository.batchInsert(prodLines);

            // 建立 source 关联记录
            buildAndSaveSources(prodOrder, prodLines, dto.getSalesOrderId(),
                    lineIds, salesLineMap);

            // 转换为 VO
            orderVOs.add(toProductionOrderVO(prodOrder));
        }

        // 5. 构建返回结果
        SalesOrder salesOrder = salesOrderRepository.selectById(dto.getSalesOrderId());
        ConvertResultVO result = new ConvertResultVO();
        result.setProductionOrders(orderVOs);
        result.setSalesOrderId(dto.getSalesOrderId());
        result.setSalesOrderNo(salesOrder != null ? salesOrder.getOrderNo() : null);
        result.setSalesOrderStatus(salesOrder != null && salesOrder.getStatus() != null
                ? salesOrder.getStatus().name() : null);
        result.setSalesOrderStatusLabel(salesOrder != null && salesOrder.getStatus() != null
                ? salesOrder.getStatus().getLabel() : null);

        log.info("订单转化应用服务完成: salesOrderId={}, 生成{}张生产订单",
                dto.getSalesOrderId(), orderVOs.size());

        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 建立并保存生产订单与销售订单的关联记录
     * <p>
     * 通过 spuId+colorWayId 匹配生产订单行和销售订单行，
     * 计算每行的可转化数量（行总数量 - 转化前已分配数量），
     * 然后创建 source 关联记录并持久化。
     * </p>
     *
     * @param prodOrder    生产订单
     * @param prodLines    生产订单行列表
     * @param salesOrderId 销售订单ID
     * @param lineIds      选中的销售订单行ID列表
     * @param salesLineMap 销售订单行快照（key=行ID）
     */
    private void buildAndSaveSources(ProductionOrder prodOrder, List<ProductionOrderLine> prodLines,
                                      Long salesOrderId, List<Long> lineIds,
                                      Map<Long, SalesOrderLine> salesLineMap) {
        // 按 spuId+colorWayId 索引生产订单行
        Map<String, ProductionOrderLine> prodLineMap = new HashMap<>();
        for (ProductionOrderLine pl : prodLines) {
            prodLineMap.put(pl.getSpuId() + "_" + pl.getColorWayId(), pl);
        }

        List<ProductionOrderSource> sources = new ArrayList<>();
        for (Long lineId : lineIds) {
            SalesOrderLine sLine = salesLineMap.get(lineId);
            if (sLine == null) continue;

            String key = sLine.getSpuId() + "_" + sLine.getColorWayId();
            ProductionOrderLine matchedProdLine = prodLineMap.get(key);
            if (matchedProdLine == null) continue;

            // 计算本次分配量 = 行总数量 - 之前已分配量
            // 注意：DomainService 在转化前已校验未全额转化，这里获取的是转化前的已分配量
            int previousAllocated = orderConvertDomainService.getAllocatedQuantity(lineId);
            int allocated = sLine.getTotalQuantity() - previousAllocated;
            if (allocated <= 0) continue;

            ProductionOrderSource source = new ProductionOrderSource();
            source.setProductionOrderId(prodOrder.getId());
            source.setProductionLineId(matchedProdLine.getId());
            source.setSalesOrderId(salesOrderId);
            source.setSalesLineId(lineId);
            source.setAllocatedQuantity(allocated);
            sources.add(source);
        }

        if (!sources.isEmpty()) {
            productionOrderSourceRepository.batchInsert(sources);
        }
    }

    /**
     * 将生产订单实体转换为 VO
     */
    private ProductionOrderVO toProductionOrderVO(ProductionOrder order) {
        ProductionOrderVO vo = new ProductionOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPlanDate(order.getPlanDate() != null ? order.getPlanDate().toString() : null);
        vo.setDeadlineDate(order.getDeadlineDate() != null ? order.getDeadlineDate().toString() : null);
        vo.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        vo.setStatusLabel(order.getStatus() != null ? order.getStatus().getLabel() : null);
        vo.setSourceType(order.getSourceType());
        vo.setWorkshopId(order.getWorkshopId());
        vo.setTotalQuantity(order.getTotalQuantity());
        vo.setCompletedQuantity(order.getCompletedQuantity());
        vo.setStockedQuantity(order.getStockedQuantity());
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());

        if (order.getLines() != null && !order.getLines().isEmpty()) {
            vo.setLines(order.getLines().stream()
                    .map(this::toProductionOrderLineVO)
                    .toList());
        } else {
            vo.setLines(List.of());
        }

        return vo;
    }

    /**
     * 将生产订单行实体转换为 VO
     */
    private ProductionOrderLineVO toProductionOrderLineVO(ProductionOrderLine line) {
        ProductionOrderLineVO vo = new ProductionOrderLineVO();
        vo.setId(line.getId());
        vo.setLineNo(line.getLineNo());
        vo.setSpuId(line.getSpuId());
        vo.setColorWayId(line.getColorWayId());
        vo.setBomId(line.getBomId());

        if (line.getSpuId() != null) {
            Spu spu = spuRepository.selectById(line.getSpuId());
            if (spu != null) {
                vo.setSpuCode(spu.getCode());
                vo.setSpuName(spu.getName());
            }
        }
        if (line.getColorWayId() != null) {
            ColorWay colorWay = colorWayRepository.selectById(line.getColorWayId());
            if (colorWay != null) {
                vo.setColorName(colorWay.getColorName());
                vo.setColorCode(colorWay.getColorCode());
            }
        }

        if (line.getSizeMatrix() != null) {
            var matrix = line.getSizeMatrix();
            vo.setSizeMatrix(Map.of(
                    "sizeGroupId", matrix.getSizeGroupId(),
                    "sizes", matrix.getSizes(),
                    "totalQuantity", matrix.getTotalQuantity()
            ));
        }

        vo.setTotalQuantity(line.getTotalQuantity());
        vo.setCompletedQuantity(line.getCompletedQuantity());
        vo.setStockedQuantity(line.getStockedQuantity());
        vo.setSkipCutting(line.getSkipCutting());
        vo.setStatus(line.getStatus() != null ? line.getStatus().name() : null);
        vo.setStatusLabel(line.getStatus() != null ? line.getStatus().getLabel() : null);
        vo.setRemark(line.getRemark());

        return vo;
    }
}
