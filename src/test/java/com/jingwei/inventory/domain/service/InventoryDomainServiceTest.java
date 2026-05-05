package com.jingwei.inventory.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.inventory.domain.repository.InventoryOperationRepository;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 库存领域服务单元测试
 * <p>
 * 覆盖 T-29 验收标准的核心功能：
 * <ul>
 *   <li>12种操作类型的字段变更规则</li>
 *   <li>库存不足校验</li>
 *   <li>乐观锁重试机制</li>
 *   <li>操作流水记录</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class InventoryDomainServiceTest {

    @Mock
    private InventorySkuRepository inventorySkuRepository;
    @Mock
    private InventoryMaterialRepository inventoryMaterialRepository;
    @Mock
    private InventoryOperationRepository inventoryOperationRepository;

    private InventoryDomainService service;

    @BeforeEach
    void setUp() {
        service = new InventoryDomainService(
                inventorySkuRepository, inventoryMaterialRepository, inventoryOperationRepository);
    }

    // ==================== 辅助方法 ====================

    private InventorySku buildSkuRecord(int available, int locked, int qc) {
        InventorySku record = new InventorySku();
        record.setId(1L);
        record.setSkuId(100L);
        record.setWarehouseId(1L);
        record.setBatchNo("B20260501");
        record.setAvailableQty(available);
        record.setLockedQty(locked);
        record.setQcQty(qc);
        record.setTotalQty(available + locked + qc);
        record.setVersion(0);
        return record;
    }

    private ChangeInventoryCommand buildSkuCommand(OperationType opType, int qty) {
        return ChangeInventoryCommand.forSku(
                opType, 1L, 100L, 1L, "B20260501", qty,
                "SALES_ORDER", 10L, 1L);
    }

    // ==================== ALLOCATE（锁定预留） ====================

    @Nested
    @DisplayName("ALLOCATE — 锁定预留")
    class AllocateTests {

        @Test
        @DisplayName("正常预留 → available 减少, locked 增加, total 不变")
        void allocate_shouldMoveFromAvailableToLocked() {
            InventorySku record = buildSkuRecord(100, 0, 0);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);
            when(inventorySkuRepository.updateById(any())).thenReturn(1);

            service.changeInventory(buildSkuCommand(OperationType.ALLOCATE, 30));

            assertEquals(70, record.getAvailableQty());
            assertEquals(30, record.getLockedQty());
            assertEquals(0, record.getQcQty());
            assertEquals(100, record.getTotalQty());

            verify(inventoryOperationRepository).insert(argThat(op ->
                    op.getOperationType() == OperationType.ALLOCATE
                            && op.getQuantity().intValue() == 30
                            && op.getAvailableBefore().intValue() == 100
                            && op.getAvailableAfter().intValue() == 70
                            && op.getLockedBefore().intValue() == 0
                            && op.getLockedAfter().intValue() == 30));
        }

        @Test
        @DisplayName("库存不足时预留 → 抛 InsufficientInventory")
        void allocate_insufficient_shouldThrow() {
            InventorySku record = buildSkuRecord(20, 0, 0);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);

            BizException ex = assertThrows(BizException.class, () ->
                    service.changeInventory(buildSkuCommand(OperationType.ALLOCATE, 50)));
            assertEquals(ErrorCode.INSUFFICIENT_INVENTORY.getCode(), ex.getCode());
        }
    }

    // ==================== RELEASE（释放预留） ====================

    @Nested
    @DisplayName("RELEASE — 释放预留")
    class ReleaseTests {

        @Test
        @DisplayName("正常释放 → available 增加, locked 减少, total 不变")
        void release_shouldMoveFromLockedToAvailable() {
            InventorySku record = buildSkuRecord(70, 30, 0);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);
            when(inventorySkuRepository.updateById(any())).thenReturn(1);

            service.changeInventory(buildSkuCommand(OperationType.RELEASE, 30));

            assertEquals(100, record.getAvailableQty());
            assertEquals(0, record.getLockedQty());
            assertEquals(100, record.getTotalQty());
        }
    }

    // ==================== OUTBOUND_SALES（销售出库） ====================

    @Nested
    @DisplayName("OUTBOUND_SALES — 销售出库")
    class OutboundSalesTests {

        @Test
        @DisplayName("正常出库 → locked 减少, total 减少")
        void outboundSales_shouldReduceLockedAndTotal() {
            InventorySku record = buildSkuRecord(50, 30, 0);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);
            when(inventorySkuRepository.updateById(any())).thenReturn(1);

            service.changeInventory(buildSkuCommand(OperationType.OUTBOUND_SALES, 20));

            assertEquals(50, record.getAvailableQty());
            assertEquals(10, record.getLockedQty());
            assertEquals(60, record.getTotalQty());
        }

        @Test
        @DisplayName("锁定库存不足 → 抛异常")
        void outboundSales_insufficientLocked_shouldThrow() {
            InventorySku record = buildSkuRecord(50, 10, 0);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);

            BizException ex = assertThrows(BizException.class, () ->
                    service.changeInventory(buildSkuCommand(OperationType.OUTBOUND_SALES, 20)));
            assertEquals(ErrorCode.LOCKED_INVENTORY_INSUFFICIENT.getCode(), ex.getCode());
        }
    }

    // ==================== INBOUND_PURCHASE（采购入库） ====================

    @Nested
    @DisplayName("INBOUND_PURCHASE — 采购入库")
    class InboundPurchaseTests {

        @Test
        @DisplayName("采购入库 → qc 增加, total 增加")
        void inboundPurchase_shouldIncreaseQc() {
            InventorySku record = buildSkuRecord(50, 20, 0);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);
            when(inventorySkuRepository.updateById(any())).thenReturn(1);

            service.changeInventory(buildSkuCommand(OperationType.INBOUND_PURCHASE, 100));

            assertEquals(50, record.getAvailableQty());
            assertEquals(20, record.getLockedQty());
            assertEquals(100, record.getQcQty());
            assertEquals(170, record.getTotalQty());
        }
    }

    // ==================== INBOUND_PRODUCTION（生产入库） ====================

    @Nested
    @DisplayName("INBOUND_PRODUCTION — 生产入库")
    class InboundProductionTests {

        @Test
        @DisplayName("生产入库 → available 增加, total 增加")
        void inboundProduction_shouldIncreaseAvailable() {
            InventorySku record = buildSkuRecord(50, 0, 0);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);
            when(inventorySkuRepository.updateById(any())).thenReturn(1);

            service.changeInventory(buildSkuCommand(OperationType.INBOUND_PRODUCTION, 200));

            assertEquals(250, record.getAvailableQty());
            assertEquals(250, record.getTotalQty());
        }
    }

    // ==================== QC_PASS / QC_FAIL ====================

    @Nested
    @DisplayName("质检操作")
    class QcTests {

        @Test
        @DisplayName("QC_PASS → qc 减少, available 增加, total 不变")
        void qcPass_shouldMoveFromQcToAvailable() {
            InventorySku record = buildSkuRecord(50, 0, 100);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);
            when(inventorySkuRepository.updateById(any())).thenReturn(1);

            service.changeInventory(buildSkuCommand(OperationType.QC_PASS, 80));

            assertEquals(130, record.getAvailableQty());
            assertEquals(20, record.getQcQty());
            assertEquals(150, record.getTotalQty());
        }

        @Test
        @DisplayName("QC_FAIL → qc 减少, total 减少")
        void qcFail_shouldReduceQcAndTotal() {
            InventorySku record = buildSkuRecord(50, 0, 100);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);
            when(inventorySkuRepository.updateById(any())).thenReturn(1);

            service.changeInventory(buildSkuCommand(OperationType.QC_FAIL, 30));

            assertEquals(50, record.getAvailableQty());
            assertEquals(70, record.getQcQty());
            assertEquals(120, record.getTotalQty());
        }

        @Test
        @DisplayName("QC_PASS 质检库存不足 → 抛异常")
        void qcPass_insufficientQc_shouldThrow() {
            InventorySku record = buildSkuRecord(50, 0, 10);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);

            BizException ex = assertThrows(BizException.class, () ->
                    service.changeInventory(buildSkuCommand(OperationType.QC_PASS, 50)));
            assertEquals(ErrorCode.QC_INVENTORY_INSUFFICIENT.getCode(), ex.getCode());
        }
    }

    // ==================== OUTBOUND_MATERIAL（领料出库） ====================

    @Nested
    @DisplayName("OUTBOUND_MATERIAL — 领料出库")
    class OutboundMaterialTests {

        @Test
        @DisplayName("领料出库 → available 减少, total 减少")
        void outboundMaterial_shouldReduceAvailable() {
            InventorySku record = buildSkuRecord(100, 0, 0);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);
            when(inventorySkuRepository.updateById(any())).thenReturn(1);

            service.changeInventory(buildSkuCommand(OperationType.OUTBOUND_MATERIAL, 40));

            assertEquals(60, record.getAvailableQty());
            assertEquals(60, record.getTotalQty());
        }

        @Test
        @DisplayName("领料库存不足 → 抛异常")
        void outboundMaterial_insufficient_shouldThrow() {
            InventorySku record = buildSkuRecord(20, 0, 0);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);

            BizException ex = assertThrows(BizException.class, () ->
                    service.changeInventory(buildSkuCommand(OperationType.OUTBOUND_MATERIAL, 50)));
            assertEquals(ErrorCode.INSUFFICIENT_INVENTORY.getCode(), ex.getCode());
        }
    }

    // ==================== ADJUST_GAIN / ADJUST_LOSS ====================

    @Nested
    @DisplayName("盘点调整")
    class AdjustTests {

        @Test
        @DisplayName("ADJUST_GAIN → available 增加, total 增加")
        void adjustGain_shouldIncreaseAvailable() {
            InventorySku record = buildSkuRecord(100, 0, 0);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);
            when(inventorySkuRepository.updateById(any())).thenReturn(1);

            service.changeInventory(buildSkuCommand(OperationType.ADJUST_GAIN, 5));

            assertEquals(105, record.getAvailableQty());
            assertEquals(105, record.getTotalQty());
        }

        @Test
        @DisplayName("ADJUST_LOSS → available 减少, total 减少")
        void adjustLoss_shouldDecreaseAvailable() {
            InventorySku record = buildSkuRecord(100, 0, 0);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);
            when(inventorySkuRepository.updateById(any())).thenReturn(1);

            service.changeInventory(buildSkuCommand(OperationType.ADJUST_LOSS, 3));

            assertEquals(97, record.getAvailableQty());
            assertEquals(97, record.getTotalQty());
        }
    }

    // ==================== 乐观锁重试 ====================

    @Nested
    @DisplayName("乐观锁重试")
    class OptimisticLockTests {

        @Test
        @DisplayName("乐观锁冲突 → 重试成功")
        void optimisticLock_retryShouldSucceed() {
            InventorySku record = buildSkuRecord(100, 0, 0);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);
            // 第一次更新失败（返回0行），第二次成功
            when(inventorySkuRepository.updateById(any()))
                    .thenReturn(0)
                    .thenReturn(1);

            // 第一次 selectById 返回原始记录
            InventorySku record2 = buildSkuRecord(100, 0, 0);
            when(inventorySkuRepository.selectById(1L))
                    .thenReturn(record)
                    .thenReturn(record2);

            service.changeInventory(buildSkuCommand(OperationType.ALLOCATE, 30));

            // 验证更新了2次（第一次失败，第二次成功）
            verify(inventorySkuRepository, times(2)).updateById(any());
        }

        @Test
        @DisplayName("3次重试仍失败 → 抛 ConcurrentConflict")
        void optimisticLock_allRetriesFailed_shouldThrow() {
            InventorySku record = buildSkuRecord(100, 0, 0);
            when(inventorySkuRepository.selectById(1L)).thenReturn(record);
            when(inventorySkuRepository.updateById(any())).thenReturn(0);

            BizException ex = assertThrows(BizException.class, () ->
                    service.changeInventory(buildSkuCommand(OperationType.ALLOCATE, 30)));
            assertEquals(ErrorCode.CONCURRENT_CONFLICT.getCode(), ex.getCode());
        }
    }

    // ==================== 库存记录不存在 ====================

    @Test
    @DisplayName("库存记录不存在 → 抛 INVENTORY_NOT_FOUND")
    void inventoryNotFound_shouldThrow() {
        when(inventorySkuRepository.selectById(999L)).thenReturn(null);

        ChangeInventoryCommand cmd = ChangeInventoryCommand.forSku(
                OperationType.ALLOCATE, 999L, 100L, 1L, "", 30,
                "SALES_ORDER", 10L, 1L);

        BizException ex = assertThrows(BizException.class, () ->
                service.changeInventory(cmd));
        assertEquals(ErrorCode.INVENTORY_NOT_FOUND.getCode(), ex.getCode());
    }
}
