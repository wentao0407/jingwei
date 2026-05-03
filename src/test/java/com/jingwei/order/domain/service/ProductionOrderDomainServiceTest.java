package com.jingwei.order.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.statemachine.StateMachine;
import com.jingwei.order.domain.model.*;
import com.jingwei.order.domain.repository.ProductionOrderLineRepository;
import com.jingwei.order.domain.repository.ProductionOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 生产订单领域服务单元测试
 * <p>
 * 覆盖 T-22 验收标准：
 * <ul>
 *   <li>生产订单可独立创建</li>
 *   <li>每行有独立状态</li>
 *   <li>主表状态取所有行的最滞后状态</li>
 *   <li>skip_cutting 标记正确存储</li>
 *   <li>行尺码矩阵正确存储</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class ProductionOrderDomainServiceTest {

    @Mock
    private ProductionOrderRepository productionOrderRepository;
    @Mock
    private ProductionOrderLineRepository productionOrderLineRepository;
    @Mock
    private StateMachine<ProductionOrderStatus, ProductionOrderEvent> stateMachine;

    private ProductionOrderDomainService service;

    @BeforeEach
    void setUp() {
        service = new ProductionOrderDomainService(
                productionOrderRepository, productionOrderLineRepository, stateMachine);
    }

    // ==================== 辅助方法 ====================

    private ProductionOrder buildOrder() {
        ProductionOrder order = new ProductionOrder();
        order.setOrderNo("MO-202605-00001");
        order.setSourceType("MANUAL");
        return order;
    }

    private ProductionOrderLine buildLine(Long spuId, Long colorWayId, int[] quantities) {
        ProductionOrderLine line = new ProductionOrderLine();
        line.setSpuId(spuId);
        line.setColorWayId(colorWayId);

        List<SizeMatrix.SizeEntry> sizes = new ArrayList<>();
        String[] codes = {"S", "M", "L"};
        for (int i = 0; i < quantities.length; i++) {
            sizes.add(new SizeMatrix.SizeEntry((long) (i + 10), codes[i], quantities[i]));
        }
        line.setSizeMatrix(new SizeMatrix(1L, sizes));
        line.setSkipCutting(false);
        return line;
    }

    private List<ProductionOrderLine> buildTwoLines() {
        return List.of(
                buildLine(201L, 301L, new int[]{100, 200, 300}),
                buildLine(202L, 302L, new int[]{50, 100, 150})
        );
    }

    // ==================== 创建订单 ====================

    @Nested
    @DisplayName("创建生产订单")
    class CreateOrder {

        @Test
        @DisplayName("正常创建 → 数量汇总正确")
        void shouldCreateOrderAndCalculateQuantities() {
            when(productionOrderRepository.insert(any())).thenReturn(1);
            when(productionOrderLineRepository.batchInsert(any())).thenReturn(2);

            ProductionOrder order = buildOrder();
            List<ProductionOrderLine> lines = buildTwoLines();
            ProductionOrder saved = service.createOrder(order, lines);

            assertEquals(ProductionOrderStatus.DRAFT, saved.getStatus());
            assertEquals(900, saved.getTotalQuantity()); // 600 + 300
            assertEquals(2, saved.getLines().size());
            assertEquals(1, saved.getLines().get(0).getLineNo());
            assertEquals(2, saved.getLines().get(1).getLineNo());
        }

        @Test
        @DisplayName("行为空 → 抛 ORDER_LINE_EMPTY")
        void shouldThrowWhenLinesIsEmpty() {
            ProductionOrder order = buildOrder();
            BizException ex = assertThrows(BizException.class,
                    () -> service.createOrder(order, List.of()));
            assertEquals(ErrorCode.ORDER_LINE_EMPTY.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("行重复（同spuId+colorWayId）→ 抛异常")
        void shouldThrowWhenDuplicateSpuAndColor() {
            ProductionOrder order = buildOrder();
            List<ProductionOrderLine> lines = List.of(
                    buildLine(201L, 301L, new int[]{100, 200, 300}),
                    buildLine(201L, 301L, new int[]{50, 60, 70})
            );
            BizException ex = assertThrows(BizException.class,
                    () -> service.createOrder(order, lines));
            assertEquals(ErrorCode.ORDER_LINE_DUPLICATE.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("skip_cutting 标记正确存储")
        void shouldPreserveSkipCuttingFlag() {
            when(productionOrderRepository.insert(any())).thenReturn(1);
            when(productionOrderLineRepository.batchInsert(any())).thenReturn(1);

            ProductionOrder order = buildOrder();
            ProductionOrderLine line = buildLine(201L, 301L, new int[]{100, 200, 300});
            line.setSkipCutting(true);

            ProductionOrder saved = service.createOrder(order, List.of(line));
            assertTrue(saved.getLines().get(0).getSkipCutting());
        }

        @Test
        @DisplayName("尺码矩阵正确存储")
        void shouldStoreSizeMatrix() {
            when(productionOrderRepository.insert(any())).thenReturn(1);
            when(productionOrderLineRepository.batchInsert(any())).thenReturn(1);

            ProductionOrder order = buildOrder();
            ProductionOrderLine line = buildLine(201L, 301L, new int[]{100, 200, 300});

            ProductionOrder saved = service.createOrder(order, List.of(line));
            SizeMatrix matrix = saved.getLines().get(0).getSizeMatrix();

            assertNotNull(matrix);
            assertEquals(1L, matrix.getSizeGroupId());
            assertEquals(3, matrix.getSizes().size());
            assertEquals(600, matrix.getTotalQuantity());
        }
    }

    // ==================== 编辑订单 ====================

    @Nested
    @DisplayName("编辑生产订单")
    class UpdateOrder {

        @Test
        @DisplayName("非DRAFT状态编辑 → 抛异常")
        void shouldRejectEditWhenNotDraft() {
            ProductionOrder existing = buildOrder();
            existing.setStatus(ProductionOrderStatus.RELEASED);
            when(productionOrderRepository.selectById(anyLong())).thenReturn(existing);

            BizException ex = assertThrows(BizException.class,
                    () -> service.updateOrder(1L, buildOrder(), buildTwoLines(), 100L));
            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("DRAFT状态编辑 → 成功")
        void shouldAllowEditWhenDraft() {
            ProductionOrder existing = buildOrder();
            existing.setStatus(ProductionOrderStatus.DRAFT);
            existing.setId(1L);
            when(productionOrderRepository.selectById(anyLong())).thenReturn(existing);
            when(productionOrderRepository.updateById(any())).thenReturn(1);
            when(productionOrderLineRepository.deleteByOrderId(anyLong())).thenReturn(2);
            when(productionOrderLineRepository.batchInsert(any())).thenReturn(2);
            when(productionOrderRepository.selectDetailById(anyLong())).thenReturn(existing);

            ProductionOrder update = buildOrder();
            assertDoesNotThrow(() -> service.updateOrder(1L, update, buildTwoLines(), 100L));
        }

        @Test
        @DisplayName("订单不存在 → 抛异常")
        void shouldThrowWhenOrderNotFound() {
            when(productionOrderRepository.selectById(anyLong())).thenReturn(null);

            assertThrows(BizException.class,
                    () -> service.updateOrder(999L, buildOrder(), buildTwoLines(), 100L));
        }
    }

    // ==================== 删除订单 ====================

    @Nested
    @DisplayName("删除生产订单")
    class DeleteOrder {

        @Test
        @DisplayName("DRAFT状态删除 → 成功")
        void shouldAllowDeleteWhenDraft() {
            ProductionOrder existing = buildOrder();
            existing.setStatus(ProductionOrderStatus.DRAFT);
            when(productionOrderRepository.selectById(anyLong())).thenReturn(existing);
            when(productionOrderLineRepository.deleteByOrderId(anyLong())).thenReturn(2);
            when(productionOrderRepository.deleteById(anyLong())).thenReturn(1);

            assertDoesNotThrow(() -> service.deleteOrder(1L));
        }

        @Test
        @DisplayName("非DRAFT状态删除 → 抛异常")
        void shouldRejectDeleteWhenNotDraft() {
            ProductionOrder existing = buildOrder();
            existing.setStatus(ProductionOrderStatus.RELEASED);
            when(productionOrderRepository.selectById(anyLong())).thenReturn(existing);

            BizException ex = assertThrows(BizException.class,
                    () -> service.deleteOrder(1L));
            assertEquals(ErrorCode.OPERATION_NOT_ALLOWED.getCode(), ex.getCode());
        }
    }

    // ==================== 主表状态计算 ====================

    @Nested
    @DisplayName("主表状态计算")
    class MainStatusCalculation {

        @Test
        @DisplayName("多行不同状态 → 主表取最滞后状态")
        void shouldCalculateMostBehindStatus() {
            ProductionOrder order = buildOrder();
            order.setId(1L);
            order.setStatus(ProductionOrderStatus.SEWING);

            ProductionOrderLine line1 = buildLine(201L, 301L, new int[]{100, 200, 300});
            line1.setStatus(ProductionOrderStatus.SEWING);
            ProductionOrderLine line2 = buildLine(202L, 302L, new int[]{50, 100, 150});
            line2.setStatus(ProductionOrderStatus.RELEASED);

            when(productionOrderRepository.selectById(1L)).thenReturn(order);
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line1, line2));
            when(productionOrderRepository.updateById(any())).thenReturn(1);

            service.recalculateMainStatus(1L);

            // 验证主表状态更新为最滞后的 RELEASED
            verify(productionOrderRepository).updateById(argThat(o ->
                    o.getStatus() == ProductionOrderStatus.RELEASED));
        }

        @Test
        @DisplayName("所有行同状态 → 主表状态不变")
        void shouldNotUpdateWhenAllLinesSameStatus() {
            ProductionOrder order = buildOrder();
            order.setId(1L);
            order.setStatus(ProductionOrderStatus.SEWING);

            ProductionOrderLine line1 = buildLine(201L, 301L, new int[]{100, 200, 300});
            line1.setStatus(ProductionOrderStatus.SEWING);
            ProductionOrderLine line2 = buildLine(202L, 302L, new int[]{50, 100, 150});
            line2.setStatus(ProductionOrderStatus.SEWING);

            when(productionOrderRepository.selectById(1L)).thenReturn(order);
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(line1, line2));

            service.recalculateMainStatus(1L);

            // 状态相同，不应调用 updateById
            verify(productionOrderRepository, never()).updateById(any());
        }
    }
}
