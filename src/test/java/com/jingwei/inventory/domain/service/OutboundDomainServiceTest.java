package com.jingwei.inventory.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 出库单领域服务单元测试
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class OutboundDomainServiceTest {

    @Mock private OutboundOrderRepository outboundOrderRepository;
    @Mock private OutboundOrderLineRepository outboundOrderLineRepository;
    @Mock private InventorySkuRepository inventorySkuRepository;
    @Mock private InventoryMaterialRepository inventoryMaterialRepository;
    @Mock private InventoryDomainService inventoryDomainService;

    private OutboundDomainService service;

    @BeforeEach
    void setUp() {
        service = new OutboundDomainService(
                outboundOrderRepository, outboundOrderLineRepository,
                inventorySkuRepository, inventoryMaterialRepository,
                inventoryDomainService);
    }

    private OutboundOrder buildOutboundOrder(OutboundType type) {
        OutboundOrder order = new OutboundOrder();
        order.setId(1L);
        order.setOutboundNo("CK-20260505-0001");
        order.setOutboundType(type);
        order.setWarehouseId(1L);
        order.setStatus(OutboundStatus.DRAFT);
        return order;
    }

    private OutboundOrderLine buildOutboundLine(InventoryType invType, Long skuId, int qty) {
        OutboundOrderLine line = new OutboundOrderLine();
        line.setId(10L);
        line.setOutboundId(1L);
        line.setInventoryType(invType);
        line.setSkuId(skuId);
        line.setBatchNo("B001");
        line.setPlannedQty(BigDecimal.valueOf(qty));
        line.setActualQty(BigDecimal.valueOf(qty));
        return line;
    }

    @Nested
    @DisplayName("确认出库（发货确认）")
    class ConfirmShippedTests {

        @Test
        @DisplayName("销售出库确认 → 触发 OUTBOUND_SALES，locked 减少")
        void confirmSales_shouldTriggerOutboundSales() {
            OutboundOrder order = buildOutboundOrder(OutboundType.SALES);
            order.setLines(List.of(buildOutboundLine(InventoryType.SKU, 100L, 30)));

            when(outboundOrderRepository.selectDetailById(1L)).thenReturn(order);
            InventorySku sku = new InventorySku();
            sku.setId(1L);
            sku.setLockedQty(50);
            when(inventorySkuRepository.selectBySkuAndWarehouseAndBatch(100L, 1L, "B001")).thenReturn(sku);
            when(outboundOrderRepository.updateById(any())).thenReturn(1);

            service.confirmShipped(1L, 1L);

            verify(inventoryDomainService).changeInventory(argThat(cmd ->
                    cmd.getOperationType() == OperationType.OUTBOUND_SALES
                            && cmd.getQuantity().intValue() == 30));
            verify(outboundOrderRepository).updateById(argThat(o -> o.getStatus() == OutboundStatus.SHIPPED));
        }

        @Test
        @DisplayName("领料出库确认 → 触发 OUTBOUND_MATERIAL，available 减少")
        void confirmMaterial_shouldTriggerOutboundMaterial() {
            OutboundOrder order = buildOutboundOrder(OutboundType.MATERIAL);
            order.setLines(List.of(buildOutboundLine(InventoryType.SKU, 100L, 50)));

            when(outboundOrderRepository.selectDetailById(1L)).thenReturn(order);
            InventorySku sku = new InventorySku();
            sku.setId(1L);
            when(inventorySkuRepository.selectBySkuAndWarehouseAndBatch(100L, 1L, "B001")).thenReturn(sku);
            when(outboundOrderRepository.updateById(any())).thenReturn(1);

            service.confirmShipped(1L, 1L);

            verify(inventoryDomainService).changeInventory(argThat(cmd ->
                    cmd.getOperationType() == OperationType.OUTBOUND_MATERIAL));
        }

        @Test
        @DisplayName("库存记录不存在 → 抛 INVENTORY_NOT_FOUND")
        void confirmSkuNotFound_shouldThrow() {
            OutboundOrder order = buildOutboundOrder(OutboundType.SALES);
            order.setLines(List.of(buildOutboundLine(InventoryType.SKU, 100L, 30)));

            when(outboundOrderRepository.selectDetailById(1L)).thenReturn(order);
            when(inventorySkuRepository.selectBySkuAndWarehouseAndBatch(100L, 1L, "B001")).thenReturn(null);

            BizException ex = assertThrows(BizException.class, () ->
                    service.confirmShipped(1L, 1L));
            assertEquals(ErrorCode.INVENTORY_NOT_FOUND.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("已 SHIPPED 状态重复确认 → 抛异常")
        void confirmAlreadyShipped_shouldThrow() {
            OutboundOrder order = buildOutboundOrder(OutboundType.SALES);
            order.setStatus(OutboundStatus.SHIPPED);
            when(outboundOrderRepository.selectDetailById(1L)).thenReturn(order);

            BizException ex = assertThrows(BizException.class, () ->
                    service.confirmShipped(1L, 1L));
            assertEquals(ErrorCode.OUTBOUND_STATUS_INVALID.getCode(), ex.getCode());
        }
    }

    @Nested
    @DisplayName("创建出库单")
    class CreateOrderTests {

        @Test
        @DisplayName("正常创建 → 状态为 DRAFT")
        void create_shouldSetDraft() {
            OutboundOrder order = buildOutboundOrder(OutboundType.SALES);
            OutboundOrderLine line = buildOutboundLine(InventoryType.SKU, 100L, 30);

            OutboundOrder result = service.createOrder(order, List.of(line));

            assertEquals(OutboundStatus.DRAFT, result.getStatus());
            verify(outboundOrderRepository).insert(any());
        }

        @Test
        @DisplayName("空行列表 → 抛异常")
        void create_emptyLines_shouldThrow() {
            OutboundOrder order = buildOutboundOrder(OutboundType.SALES);

            assertThrows(BizException.class, () -> service.createOrder(order, List.of()));
        }
    }
}
