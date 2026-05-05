package com.jingwei.inventory.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.repository.InventoryAllocationRepository;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import com.jingwei.system.domain.repository.SysConfigRepository;
import com.jingwei.system.domain.service.SysConfigDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 库存预留领域服务单元测试
 * <p>
 * 覆盖 T-30 验收标准的核心功能：
 * <ul>
 *   <li>全额预留 / 部分预留 / 无库存</li>
 *   <li>释放预留</li>
 *   <li>出库完成更新预留状态</li>
 *   <li>过期自动释放</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class AllocationDomainServiceTest {

    @Mock
    private InventorySkuRepository inventorySkuRepository;
    @Mock
    private InventoryAllocationRepository inventoryAllocationRepository;
    @Mock
    private InventoryDomainService inventoryDomainService;
    @Mock
    private SysConfigDomainService sysConfigDomainService;
    @Mock
    private SysConfigRepository sysConfigRepository;

    private AllocationDomainService service;

    @BeforeEach
    void setUp() {
        service = new AllocationDomainService(
                inventorySkuRepository, inventoryAllocationRepository,
                inventoryDomainService, sysConfigDomainService);
    }

    // ==================== 辅助方法 ====================

    private InventorySku buildSku(Long id, int available) {
        InventorySku sku = new InventorySku();
        sku.setId(id);
        sku.setSkuId(100L);
        sku.setWarehouseId(1L);
        sku.setBatchNo("B001");
        sku.setAvailableQty(available);
        sku.setLockedQty(0);
        sku.setQcQty(0);
        sku.setTotalQty(available);
        return sku;
    }

    // ==================== 预留测试 ====================

    @Nested
    @DisplayName("库存预留")
    class AllocateTests {

        @Test
        @DisplayName("全额预留 → allocated = 需求量, shortfall = 0")
        void fullAllocate_shouldAllocateAll() {
            InventorySku sku = buildSku(1L, 100);
            when(inventorySkuRepository.selectBySkuAndWarehouse(100L, 1L)).thenReturn(List.of(sku));
            when(sysConfigDomainService.getSysConfigRepository()).thenReturn(sysConfigRepository);
            when(inventoryAllocationRepository.insert(any())).thenAnswer(invocation -> {
                InventoryAllocation alloc = invocation.getArgument(0);
                alloc.setId(1L);
                return 1;
            });

            AllocationDomainService.AllocationResult result = service.allocate(
                    100L, 1L, 60, "SALES_ORDER", 10L, 20L, 1L);

            assertEquals(60, result.getAllocatedQty());
            assertEquals(0, result.getShortfall());
            assertNotNull(result.getAllocationId());

            verify(inventoryDomainService).changeInventory(any(ChangeInventoryCommand.class));
            verify(inventoryAllocationRepository).insert(any(InventoryAllocation.class));
        }

        @Test
        @DisplayName("部分预留 → allocated = 可用量, shortfall = 缺口")
        void partialAllocate_shouldAllocateAvailable() {
            InventorySku sku = buildSku(1L, 30);
            when(inventorySkuRepository.selectBySkuAndWarehouse(100L, 1L)).thenReturn(List.of(sku));
            when(sysConfigDomainService.getSysConfigRepository()).thenReturn(sysConfigRepository);
            when(inventoryAllocationRepository.insert(any())).thenAnswer(invocation -> {
                InventoryAllocation alloc = invocation.getArgument(0);
                alloc.setId(2L);
                return 1;
            });

            AllocationDomainService.AllocationResult result = service.allocate(
                    100L, 1L, 60, "SALES_ORDER", 10L, 20L, 1L);

            assertEquals(30, result.getAllocatedQty());
            assertEquals(30, result.getShortfall());
        }

        @Test
        @DisplayName("无库存 → allocated = 0, shortfall = 需求量")
        void noStock_shouldNotAllocate() {
            InventorySku sku = buildSku(1L, 0);
            when(inventorySkuRepository.selectBySkuAndWarehouse(100L, 1L)).thenReturn(List.of(sku));

            AllocationDomainService.AllocationResult result = service.allocate(
                    100L, 1L, 60, "SALES_ORDER", 10L, 20L, 1L);

            assertEquals(0, result.getAllocatedQty());
            assertEquals(60, result.getShortfall());
            assertNull(result.getAllocationId());

            verify(inventoryDomainService, never()).changeInventory(any());
        }
    }

    // ==================== 释放测试 ====================

    @Nested
    @DisplayName("释放预留")
    class ReleaseTests {

        @Test
        @DisplayName("正常释放 → remaining 清零, status = RELEASED")
        void release_shouldSetReleased() {
            InventoryAllocation allocation = new InventoryAllocation();
            allocation.setId(1L);
            allocation.setStatus(AllocationStatus.ACTIVE);
            allocation.setRemainingQty(BigDecimal.valueOf(50));
            allocation.setSkuId(100L);
            allocation.setWarehouseId(1L);
            allocation.setOrderType("SALES_ORDER");
            allocation.setOrderId(10L);

            when(inventoryAllocationRepository.selectById(1L)).thenReturn(allocation);
            InventorySku sku = buildSku(1L, 50);
            sku.setLockedQty(50);
            when(inventorySkuRepository.selectBySkuAndWarehouse(100L, 1L)).thenReturn(List.of(sku));
            when(inventoryAllocationRepository.updateById(any())).thenReturn(1);

            service.release(1L, 1L);

            assertEquals(AllocationStatus.RELEASED, allocation.getStatus());
            assertEquals(0, BigDecimal.ZERO.compareTo(allocation.getRemainingQty()));
            verify(inventoryDomainService).changeInventory(any(ChangeInventoryCommand.class));
        }

        @Test
        @DisplayName("释放已释放的预留 → 抛异常")
        void release_alreadyReleased_shouldThrow() {
            InventoryAllocation allocation = new InventoryAllocation();
            allocation.setId(1L);
            allocation.setStatus(AllocationStatus.RELEASED);

            when(inventoryAllocationRepository.selectById(1L)).thenReturn(allocation);

            BizException ex = assertThrows(BizException.class, () ->
                    service.release(1L, 1L));
            assertEquals(ErrorCode.ALLOCATION_STATUS_INVALID.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("预留记录不存在 → 抛 ALLOCATION_NOT_FOUND")
        void release_notFound_shouldThrow() {
            when(inventoryAllocationRepository.selectById(999L)).thenReturn(null);

            BizException ex = assertThrows(BizException.class, () ->
                    service.release(999L, 1L));
            assertEquals(ErrorCode.ALLOCATION_NOT_FOUND.getCode(), ex.getCode());
        }
    }

    // ==================== 出库完成测试 ====================

    @Nested
    @DisplayName("出库完成")
    class FulfillTests {

        @Test
        @DisplayName("全部出库 → status = FULFILLED, remaining = 0")
        void fulfillAll_shouldSetFulfilled() {
            InventoryAllocation allocation = new InventoryAllocation();
            allocation.setId(1L);
            allocation.setStatus(AllocationStatus.ACTIVE);
            allocation.setAllocatedQty(BigDecimal.valueOf(50));
            allocation.setFulfilledQty(BigDecimal.ZERO);
            allocation.setRemainingQty(BigDecimal.valueOf(50));

            when(inventoryAllocationRepository.selectById(1L)).thenReturn(allocation);
            when(inventoryAllocationRepository.updateById(any())).thenReturn(1);

            service.fulfill(1L, 50, 1L);

            assertEquals(AllocationStatus.FULFILLED, allocation.getStatus());
            assertEquals(0, BigDecimal.valueOf(50).compareTo(allocation.getFulfilledQty()));
            assertEquals(0, BigDecimal.ZERO.compareTo(allocation.getRemainingQty()));
        }

        @Test
        @DisplayName("部分出库 → status = PARTIAL_FULFILLED")
        void fulfillPartial_shouldSetPartialFulfilled() {
            InventoryAllocation allocation = new InventoryAllocation();
            allocation.setId(1L);
            allocation.setStatus(AllocationStatus.ACTIVE);
            allocation.setAllocatedQty(BigDecimal.valueOf(50));
            allocation.setFulfilledQty(BigDecimal.ZERO);
            allocation.setRemainingQty(BigDecimal.valueOf(50));

            when(inventoryAllocationRepository.selectById(1L)).thenReturn(allocation);
            when(inventoryAllocationRepository.updateById(any())).thenReturn(1);

            service.fulfill(1L, 20, 1L);

            assertEquals(AllocationStatus.PARTIAL_FULFILLED, allocation.getStatus());
            assertEquals(0, BigDecimal.valueOf(20).compareTo(allocation.getFulfilledQty()));
            assertEquals(0, BigDecimal.valueOf(30).compareTo(allocation.getRemainingQty()));
        }
    }

    // ==================== 过期释放测试 ====================

    @Nested
    @DisplayName("过期自动释放")
    class ExpireTests {

        @Test
        @DisplayName("过期记录 → 自动释放并标记 EXPIRED")
        void expire_shouldReleaseAndMarkExpired() {
            InventoryAllocation expired = new InventoryAllocation();
            expired.setId(1L);
            expired.setStatus(AllocationStatus.ACTIVE);
            expired.setRemainingQty(BigDecimal.valueOf(30));
            expired.setSkuId(100L);
            expired.setWarehouseId(1L);
            expired.setOrderType("SALES_ORDER");
            expired.setOrderId(10L);

            when(inventoryAllocationRepository.selectExpiredActive()).thenReturn(List.of(expired));
            when(inventoryAllocationRepository.selectById(1L)).thenReturn(expired);
            InventorySku sku = buildSku(1L, 30);
            sku.setLockedQty(30);
            when(inventorySkuRepository.selectBySkuAndWarehouse(100L, 1L)).thenReturn(List.of(sku));
            when(inventoryAllocationRepository.updateById(any())).thenReturn(1);

            int count = service.releaseExpired(1L);

            assertEquals(1, count);
            assertEquals(AllocationStatus.EXPIRED, expired.getStatus());
        }

        @Test
        @DisplayName("无过期记录 → 返回0")
        void noExpired_shouldReturnZero() {
            when(inventoryAllocationRepository.selectExpiredActive()).thenReturn(Collections.emptyList());

            int count = service.releaseExpired(1L);
            assertEquals(0, count);
        }

        @Test
        @DisplayName("过期释放重复执行 → 幂等不重复释放")
        void expire_repeatedExecution_shouldBeIdempotent() {
            // 第一次释放成功
            InventoryAllocation expired = new InventoryAllocation();
            expired.setId(1L);
            expired.setStatus(AllocationStatus.ACTIVE);
            expired.setRemainingQty(BigDecimal.valueOf(30));
            expired.setSkuId(100L);
            expired.setWarehouseId(1L);
            expired.setOrderType("SALES_ORDER");
            expired.setOrderId(10L);

            when(inventoryAllocationRepository.selectExpiredActive()).thenReturn(List.of(expired));
            when(inventoryAllocationRepository.selectById(1L)).thenReturn(expired);
            InventorySku sku = buildSku(1L, 30);
            sku.setLockedQty(30);
            when(inventorySkuRepository.selectBySkuAndWarehouse(100L, 1L)).thenReturn(List.of(sku));
            when(inventoryAllocationRepository.updateById(any())).thenReturn(1);

            int count1 = service.releaseExpired(1L);
            assertEquals(1, count1);
            assertEquals(AllocationStatus.EXPIRED, expired.getStatus());

            // 第二次执行：已释放的记录不会被再次查询到（selectExpiredActive 已过滤）
            when(inventoryAllocationRepository.selectExpiredActive()).thenReturn(Collections.emptyList());
            int count2 = service.releaseExpired(1L);
            assertEquals(0, count2);
        }
    }
}
