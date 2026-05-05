package com.jingwei.inventory.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.repository.*;
import com.jingwei.master.domain.repository.LocationRepository;
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
 * 入库单领域服务单元测试
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class InboundDomainServiceTest {

    @Mock private InboundOrderRepository inboundOrderRepository;
    @Mock private InboundOrderLineRepository inboundOrderLineRepository;
    @Mock private InventorySkuRepository inventorySkuRepository;
    @Mock private InventoryMaterialRepository inventoryMaterialRepository;
    @Mock private InventoryDomainService inventoryDomainService;
    @Mock private LocationRepository locationRepository;

    private InboundDomainService service;

    @BeforeEach
    void setUp() {
        service = new InboundDomainService(
                inboundOrderRepository, inboundOrderLineRepository,
                inventorySkuRepository, inventoryMaterialRepository,
                inventoryDomainService, locationRepository);
    }

    private InboundOrder buildInboundOrder(InboundType type) {
        InboundOrder order = new InboundOrder();
        order.setId(1L);
        order.setInboundNo("RK-20260505-0001");
        order.setInboundType(type);
        order.setWarehouseId(1L);
        order.setStatus(InboundStatus.DRAFT);
        return order;
    }

    private InboundOrderLine buildInboundLine(InventoryType invType, Long skuId, Long materialId, int qty) {
        InboundOrderLine line = new InboundOrderLine();
        line.setId(10L);
        line.setInboundId(1L);
        line.setInventoryType(invType);
        line.setSkuId(skuId);
        line.setMaterialId(materialId);
        line.setBatchNo("B001");
        line.setPlannedQty(BigDecimal.valueOf(qty));
        line.setActualQty(BigDecimal.valueOf(qty));
        line.setUnitCost(BigDecimal.valueOf(50));
        return line;
    }

    @Nested
    @DisplayName("创建入库单")
    class CreateOrderTests {

        @Test
        @DisplayName("正常创建 → 状态为 DRAFT")
        void create_shouldSetDraft() {
            InboundOrder order = buildInboundOrder(InboundType.PURCHASE);
            InboundOrderLine line = buildInboundLine(InventoryType.SKU, 100L, null, 50);

            InboundOrder result = service.createOrder(order, List.of(line));

            assertEquals(InboundStatus.DRAFT, result.getStatus());
            verify(inboundOrderRepository).insert(any());
            verify(inboundOrderLineRepository).batchInsert(any());
        }

        @Test
        @DisplayName("空行列表 → 抛异常")
        void create_emptyLines_shouldThrow() {
            InboundOrder order = buildInboundOrder(InboundType.PURCHASE);

            assertThrows(BizException.class, () -> service.createOrder(order, List.of()));
        }
    }

    @Nested
    @DisplayName("确认入库")
    class ConfirmInboundTests {

        @Test
        @DisplayName("采购入库确认 → 触发 INBOUND_PURCHASE 库存操作")
        void confirmPurchase_shouldTriggerInboundPurchase() {
            InboundOrder order = buildInboundOrder(InboundType.PURCHASE);
            order.setLines(List.of(buildInboundLine(InventoryType.SKU, 100L, null, 50)));

            when(inboundOrderRepository.selectDetailById(1L)).thenReturn(order);
            InventorySku sku = new InventorySku();
            sku.setId(1L);
            when(inventorySkuRepository.selectBySkuAndWarehouseAndBatch(100L, 1L, "B001")).thenReturn(sku);
            when(inboundOrderRepository.updateById(any())).thenReturn(1);

            service.confirmInbound(1L, 1L);

            verify(inventoryDomainService).changeInventory(argThat(cmd ->
                    cmd.getOperationType() == OperationType.INBOUND_PURCHASE
                            && cmd.getQuantity().intValue() == 50));
            verify(inboundOrderRepository).updateById(argThat(o -> o.getStatus() == InboundStatus.CONFIRMED));
        }

        @Test
        @DisplayName("生产入库确认 → 触发 INBOUND_PRODUCTION 库存操作")
        void confirmProduction_shouldTriggerInboundProduction() {
            InboundOrder order = buildInboundOrder(InboundType.PRODUCTION);
            order.setLines(List.of(buildInboundLine(InventoryType.SKU, 100L, null, 200)));

            when(inboundOrderRepository.selectDetailById(1L)).thenReturn(order);
            InventorySku sku = new InventorySku();
            sku.setId(1L);
            when(inventorySkuRepository.selectBySkuAndWarehouseAndBatch(100L, 1L, "B001")).thenReturn(sku);
            when(inboundOrderRepository.updateById(any())).thenReturn(1);

            service.confirmInbound(1L, 1L);

            verify(inventoryDomainService).changeInventory(argThat(cmd ->
                    cmd.getOperationType() == OperationType.INBOUND_PRODUCTION));
        }

        @Test
        @DisplayName("非 DRAFT 状态确认 → 抛异常")
        void confirmNonDraft_shouldThrow() {
            InboundOrder order = buildInboundOrder(InboundType.PURCHASE);
            order.setStatus(InboundStatus.CONFIRMED);
            when(inboundOrderRepository.selectDetailById(1L)).thenReturn(order);

            BizException ex = assertThrows(BizException.class, () ->
                    service.confirmInbound(1L, 1L));
            assertEquals(ErrorCode.INBOUND_STATUS_INVALID.getCode(), ex.getCode());
        }
    }
}
