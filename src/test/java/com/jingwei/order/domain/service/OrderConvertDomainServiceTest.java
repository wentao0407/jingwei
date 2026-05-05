package com.jingwei.order.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.order.domain.model.OrderChangeLog;
import com.jingwei.order.domain.model.ProductionOrder;
import com.jingwei.order.domain.model.ProductionOrderSource;
import com.jingwei.order.domain.model.SalesOrder;
import com.jingwei.order.domain.model.SalesOrderLine;
import com.jingwei.order.domain.model.SalesOrderStatus;
import com.jingwei.order.domain.model.SizeMatrix;
import com.jingwei.order.domain.model.SizeMatrix.SizeEntry;
import com.jingwei.order.domain.repository.OrderChangeLogRepository;
import com.jingwei.order.domain.repository.ProductionOrderLineRepository;
import com.jingwei.order.domain.repository.ProductionOrderRepository;
import com.jingwei.order.domain.repository.ProductionOrderSourceRepository;
import com.jingwei.order.domain.repository.SalesOrderLineRepository;
import com.jingwei.order.domain.repository.SalesOrderRepository;
import com.jingwei.procurement.domain.model.Bom;
import com.jingwei.procurement.domain.repository.BomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 订单转化领域服务单元测试
 * <p>
 * 覆盖 T-24 验收标准的核心功能：
 * <ul>
 *   <li>选择单行生成生产订单 → 关联记录正确</li>
 *   <li>同款合并 → 多个来源合并为一个生产订单行</li>
 *   <li>未确认订单转化 → 抛异常</li>
 *   <li>重复转化 → 抛异常</li>
 *   <li>款式无 BOM → 抛异常</li>
 *   <li>行不属于该订单 → 抛异常</li>
 *   <li>转化后销售订单状态 = PRODUCING</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class OrderConvertDomainServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;
    @Mock
    private SalesOrderLineRepository salesOrderLineRepository;
    @Mock
    private ProductionOrderRepository productionOrderRepository;
    @Mock
    private ProductionOrderLineRepository productionOrderLineRepository;
    @Mock
    private ProductionOrderSourceRepository productionOrderSourceRepository;
    @Mock
    private OrderChangeLogRepository orderChangeLogRepository;
    @Mock
    private BomRepository bomRepository;

    private OrderConvertDomainService service;

    @BeforeEach
    void setUp() {
        service = new OrderConvertDomainService(
                salesOrderRepository, salesOrderLineRepository,
                productionOrderRepository, productionOrderLineRepository,
                productionOrderSourceRepository, orderChangeLogRepository,
                bomRepository);
    }

    // ==================== 辅助方法 ====================

    private SalesOrder buildConfirmedOrder() {
        SalesOrder order = new SalesOrder();
        order.setId(1L);
        order.setOrderNo("SO-202605-00001");
        order.setCustomerId(1L);
        order.setSeasonId(1L);
        order.setStatus(SalesOrderStatus.CONFIRMED);
        order.setOrderDate(LocalDate.of(2026, 5, 1));
        order.setDeliveryDate(LocalDate.of(2026, 6, 15));
        return order;
    }

    private SalesOrderLine buildSalesLine(Long id, Long spuId, Long colorWayId, int[] quantities) {
        SalesOrderLine line = new SalesOrderLine();
        line.setId(id);
        line.setOrderId(1L);
        line.setSpuId(spuId);
        line.setColorWayId(colorWayId);

        List<SizeEntry> sizes = new ArrayList<>();
        long sizeId = 10L;
        for (int qty : quantities) {
            sizes.add(new SizeEntry(sizeId++, "M", qty));
        }
        line.setSizeMatrix(new SizeMatrix(1L, sizes));
        line.setTotalQuantity(line.getSizeMatrix().getTotalQuantity());
        return line;
    }

    private Bom buildApprovedBom(Long spuId) {
        Bom bom = new Bom();
        bom.setId(100L);
        bom.setSpuId(spuId);
        bom.setVersion(1);
        return bom;
    }

    // ==================== 测试用例 ====================

    @Nested
    @DisplayName("正常转化场景")
    class NormalConversionTests {

        @Test
        @DisplayName("选择单行转化 → 生成1张生产订单，关联记录正确")
        void convertSingleLine_shouldCreateOneProductionOrder() {
            // given
            SalesOrder order = buildConfirmedOrder();
            SalesOrderLine line = buildSalesLine(10L, 201L, 301L, new int[]{100, 200, 300});

            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(productionOrderSourceRepository.selectBySalesLineId(10L)).thenReturn(Collections.emptyList());
            when(bomRepository.selectApprovedBySpuId(201L)).thenReturn(Optional.of(buildApprovedBom(201L)));

            // when
            Map<Long, Boolean> skipCuttingMap = new HashMap<>();
            skipCuttingMap.put(10L, false);
            List<ProductionOrder> result = service.convertToProduction(
                    1L, List.of(10L), skipCuttingMap, 1L);

            // then
            assertEquals(1, result.size());
            ProductionOrder po = result.get(0);
            assertEquals("SALES_ORDER", po.getSourceType());
            assertEquals(1, po.getLines().size());
            assertEquals(600, po.getTotalQuantity());

            // 验证销售订单状态更新为 PRODUCING
            verify(salesOrderRepository).updateById(argThat(o ->
                    o.getStatus() == SalesOrderStatus.PRODUCING));

            // 验证变更日志记录
            verify(orderChangeLogRepository).insert(any(OrderChangeLog.class));
        }

        @Test
        @DisplayName("同款合并 → 2个不同颜色行合并为1张生产订单的2行")
        void convertSameSpuLines_shouldMergeIntoOneProductionOrder() {
            // given: 同 spuId=201 的两个颜色行
            SalesOrder order = buildConfirmedOrder();
            SalesOrderLine line1 = buildSalesLine(10L, 201L, 301L, new int[]{100, 200, 300});
            SalesOrderLine line2 = buildSalesLine(11L, 201L, 302L, new int[]{50, 100, 150});

            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line1, line2));
            when(productionOrderSourceRepository.selectBySalesLineId(10L)).thenReturn(Collections.emptyList());
            when(productionOrderSourceRepository.selectBySalesLineId(11L)).thenReturn(Collections.emptyList());
            when(bomRepository.selectApprovedBySpuId(201L)).thenReturn(Optional.of(buildApprovedBom(201L)));

            // when
            Map<Long, Boolean> skipCuttingMap = new HashMap<>();
            List<ProductionOrder> result = service.convertToProduction(
                    1L, List.of(10L, 11L), skipCuttingMap, 1L);

            // then: 1张生产订单，2行（不同颜色）
            assertEquals(1, result.size());
            assertEquals(2, result.get(0).getLines().size());
            // 总数量 = 600 + 300 = 900
            assertEquals(900, result.get(0).getTotalQuantity());
        }

        @Test
        @DisplayName("不同款拆分 → 生成2张生产订单")
        void convertDifferentSpus_shouldCreateTwoProductionOrders() {
            // given: 两个不同 spuId
            SalesOrder order = buildConfirmedOrder();
            SalesOrderLine line1 = buildSalesLine(10L, 201L, 301L, new int[]{100, 200, 300});
            SalesOrderLine line2 = buildSalesLine(11L, 202L, 302L, new int[]{50, 100, 150});

            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line1, line2));
            when(productionOrderSourceRepository.selectBySalesLineId(10L)).thenReturn(Collections.emptyList());
            when(productionOrderSourceRepository.selectBySalesLineId(11L)).thenReturn(Collections.emptyList());
            when(bomRepository.selectApprovedBySpuId(201L)).thenReturn(Optional.of(buildApprovedBom(201L)));
            when(bomRepository.selectApprovedBySpuId(202L)).thenReturn(Optional.of(buildApprovedBom(202L)));

            // when
            Map<Long, Boolean> skipCuttingMap = new HashMap<>();
            List<ProductionOrder> result = service.convertToProduction(
                    1L, List.of(10L, 11L), skipCuttingMap, 1L);

            // then: 2张生产订单
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("skipCutting 标记正确传递")
        void convertWithSkipCutting_shouldPassFlag() {
            // given
            SalesOrder order = buildConfirmedOrder();
            SalesOrderLine line = buildSalesLine(10L, 201L, 301L, new int[]{100, 200, 300});

            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(productionOrderSourceRepository.selectBySalesLineId(10L)).thenReturn(Collections.emptyList());
            when(bomRepository.selectApprovedBySpuId(201L)).thenReturn(Optional.of(buildApprovedBom(201L)));

            // when
            Map<Long, Boolean> skipCuttingMap = new HashMap<>();
            skipCuttingMap.put(10L, true);
            List<ProductionOrder> result = service.convertToProduction(
                    1L, List.of(10L), skipCuttingMap, 1L);

            // then
            assertTrue(result.get(0).getLines().get(0).getSkipCutting());
        }
    }

    @Nested
    @DisplayName("异常场景")
    class ExceptionTests {

        @Test
        @DisplayName("销售订单不存在 → 抛 DATA_NOT_FOUND")
        void convertNonExistentOrder_shouldThrow() {
            when(salesOrderRepository.selectById(999L)).thenReturn(null);

            BizException ex = assertThrows(BizException.class, () ->
                    service.convertToProduction(999L, List.of(10L), new HashMap<>(), 1L));
            assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("未确认订单转化 → 抛 ORDER_NOT_CONFIRMED")
        void convertDraftOrder_shouldThrowNotConfirmed() {
            SalesOrder order = buildConfirmedOrder();
            order.setStatus(SalesOrderStatus.DRAFT);
            when(salesOrderRepository.selectById(1L)).thenReturn(order);

            BizException ex = assertThrows(BizException.class, () ->
                    service.convertToProduction(1L, List.of(10L), new HashMap<>(), 1L));
            assertEquals(ErrorCode.ORDER_NOT_CONFIRMED.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("行不属于该订单 → 抛 ORDER_LINE_NOT_BELONG")
        void convertLineNotBelong_shouldThrow() {
            SalesOrder order = buildConfirmedOrder();
            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            // 返回空列表，模拟没有行
            when(salesOrderLineRepository.selectByOrderId(1L)).thenReturn(Collections.emptyList());

            BizException ex = assertThrows(BizException.class, () ->
                    service.convertToProduction(1L, List.of(999L), new HashMap<>(), 1L));
            assertEquals(ErrorCode.ORDER_LINE_NOT_BELONG.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("款式无 BOM → 抛 ORDER_SPU_NO_BOM")
        void convertWithoutBom_shouldThrow() {
            SalesOrder order = buildConfirmedOrder();
            SalesOrderLine line = buildSalesLine(10L, 201L, 301L, new int[]{100, 200, 300});

            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(productionOrderSourceRepository.selectBySalesLineId(10L)).thenReturn(Collections.emptyList());
            when(bomRepository.selectApprovedBySpuId(201L)).thenReturn(Optional.empty());

            BizException ex = assertThrows(BizException.class, () ->
                    service.convertToProduction(1L, List.of(10L), new HashMap<>(), 1L));
            assertEquals(ErrorCode.ORDER_SPU_NO_BOM.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("重复转化（已全额分配） → 抛 ORDER_ALREADY_CONVERTED")
        void convertAlreadyConverted_shouldThrow() {
            SalesOrder order = buildConfirmedOrder();
            SalesOrderLine line = buildSalesLine(10L, 201L, 301L, new int[]{100, 200, 300});

            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));

            // 模拟已分配600件（等于行总数量）
            ProductionOrderSource existingSource = new ProductionOrderSource();
            existingSource.setAllocatedQuantity(600);
            when(productionOrderSourceRepository.selectBySalesLineId(10L))
                    .thenReturn(List.of(existingSource));

            BizException ex = assertThrows(BizException.class, () ->
                    service.convertToProduction(1L, List.of(10L), new HashMap<>(), 1L));
            assertEquals(ErrorCode.ORDER_ALREADY_CONVERTED.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("部分转化 → 剩余部分可继续转化")
        void convertPartially_shouldAllowRemaining() {
            SalesOrder order = buildConfirmedOrder();
            SalesOrderLine line = buildSalesLine(10L, 201L, 301L, new int[]{100, 200, 300});

            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));

            // 已分配300件，行总数量600，还剩300可转化
            ProductionOrderSource existingSource = new ProductionOrderSource();
            existingSource.setAllocatedQuantity(300);
            when(productionOrderSourceRepository.selectBySalesLineId(10L))
                    .thenReturn(List.of(existingSource));
            when(bomRepository.selectApprovedBySpuId(201L)).thenReturn(Optional.of(buildApprovedBom(201L)));

            // when
            List<ProductionOrder> result = service.convertToProduction(
                    1L, List.of(10L), new HashMap<>(), 1L);

            // then: 成功转化
            assertEquals(1, result.size());
            assertEquals(1, result.get(0).getLines().size());
        }
    }

    @Nested
    @DisplayName("状态变更验证")
    class StatusChangeTests {

        @Test
        @DisplayName("转化后销售订单状态 = PRODUCING")
        void convert_shouldUpdateSalesOrderToProducing() {
            SalesOrder order = buildConfirmedOrder();
            SalesOrderLine line = buildSalesLine(10L, 201L, 301L, new int[]{100, 200, 300});

            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(productionOrderSourceRepository.selectBySalesLineId(10L)).thenReturn(Collections.emptyList());
            when(bomRepository.selectApprovedBySpuId(201L)).thenReturn(Optional.of(buildApprovedBom(201L)));

            service.convertToProduction(1L, List.of(10L), new HashMap<>(), 1L);

            verify(salesOrderRepository).updateById(argThat(o ->
                    o.getStatus() == SalesOrderStatus.PRODUCING));
        }

        @Test
        @DisplayName("变更日志记录了状态变更")
        void convert_shouldLogStatusChange() {
            SalesOrder order = buildConfirmedOrder();
            SalesOrderLine line = buildSalesLine(10L, 201L, 301L, new int[]{100, 200, 300});

            when(salesOrderRepository.selectById(1L)).thenReturn(order);
            when(salesOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line));
            when(productionOrderSourceRepository.selectBySalesLineId(10L)).thenReturn(Collections.emptyList());
            when(bomRepository.selectApprovedBySpuId(201L)).thenReturn(Optional.of(buildApprovedBom(201L)));

            service.convertToProduction(1L, List.of(10L), new HashMap<>(), 1L);

            verify(orderChangeLogRepository).insert(argThat(log ->
                    "STATUS_CHANGE".equals(log.getChangeType())
                            && "CONFIRMED".equals(log.getOldValue())
                            && "PRODUCING".equals(log.getNewValue())));
        }
    }

    @Nested
    @DisplayName("getAllocatedQuantity 测试")
    class AllocatedQuantityTests {

        @Test
        @DisplayName("无已分配记录 → 返回0")
        void noExistingSources_shouldReturnZero() {
            when(productionOrderSourceRepository.selectBySalesLineId(10L))
                    .thenReturn(Collections.emptyList());

            int result = service.getAllocatedQuantity(10L);
            assertEquals(0, result);
        }

        @Test
        @DisplayName("多条已分配记录 → 返回总和")
        void multipleSources_shouldReturnSum() {
            ProductionOrderSource s1 = new ProductionOrderSource();
            s1.setAllocatedQuantity(200);
            ProductionOrderSource s2 = new ProductionOrderSource();
            s2.setAllocatedQuantity(150);

            when(productionOrderSourceRepository.selectBySalesLineId(10L))
                    .thenReturn(List.of(s1, s2));

            int result = service.getAllocatedQuantity(10L);
            assertEquals(350, result);
        }
    }
}
