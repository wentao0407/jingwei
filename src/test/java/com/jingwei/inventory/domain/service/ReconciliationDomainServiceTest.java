package com.jingwei.inventory.domain.service;

import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import com.jingwei.inventory.domain.repository.ReconciliationRepository;
import com.jingwei.inventory.infrastructure.persistence.ReconciliationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 日终对账领域服务单元测试
 * <p>
 * 覆盖 T-41 验收标准：
 * <ul>
 *   <li>操作流水汇总与库存余额一致 → 不生成异常</li>
 *   <li>操作流水汇总与库存余额不一致 → 生成对账异常记录</li>
 *   <li>同账期重复对账幂等</li>
 *   <li>无流水但有期初/期末库存的场景</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class ReconciliationDomainServiceTest {

    @Mock
    private ReconciliationRepository reconciliationRepository;
    @Mock
    private ReconciliationMapper reconciliationMapper;
    @Mock
    private InventorySkuRepository inventorySkuRepository;
    @Mock
    private InventoryMaterialRepository inventoryMaterialRepository;

    @InjectMocks
    private ReconciliationDomainService service;

    private LocalDate accountDate;

    @BeforeEach
    void setUp() {
        accountDate = LocalDate.of(2026, 5, 4);
    }

    @Test
    @DisplayName("库存一致 → 不生成异常")
    void reconcile_consistent_shouldNotGenerateAnomaly() {
        when(reconciliationRepository.existsByAccountDate(accountDate)).thenReturn(false);

        // 无操作流水
        when(reconciliationMapper.selectSkuOpsNetChangeByDate(accountDate)).thenReturn(Collections.emptyList());
        when(reconciliationMapper.selectMaterialOpsNetChangeByDate(accountDate)).thenReturn(Collections.emptyList());

        // 库存记录 total = available + locked + qc（一致）
        InventorySku sku = buildSkuInventory(1L, 100, 20, 5, 125);
        when(inventorySkuRepository.selectAll()).thenReturn(List.of(sku));
        when(inventoryMaterialRepository.selectAll()).thenReturn(Collections.emptyList());

        int count = service.reconcile(accountDate);

        assertEquals(0, count);
        verify(reconciliationRepository, never()).insertBatch(any());
    }

    @Test
    @DisplayName("库存不一致 → 生成异常记录")
    void reconcile_inconsistent_shouldGenerateAnomaly() {
        when(reconciliationRepository.existsByAccountDate(accountDate)).thenReturn(false);
        // 提供 inventory_id=1 的操作流水，使对账循环能检查到该记录
        when(reconciliationMapper.selectSkuOpsNetChangeByDate(accountDate))
                .thenReturn(Collections.singletonList(new Object[]{1L, BigDecimal.valueOf(5)}));
        when(reconciliationMapper.selectMaterialOpsNetChangeByDate(accountDate)).thenReturn(Collections.emptyList());

        // total != available + locked + qc（不一致）
        InventorySku sku = buildSkuInventory(1L, 100, 20, 5, 130);
        when(inventorySkuRepository.selectAll()).thenReturn(List.of(sku));
        when(inventoryMaterialRepository.selectAll()).thenReturn(Collections.emptyList());

        int count = service.reconcile(accountDate);

        assertEquals(1, count);
        verify(reconciliationRepository).insertBatch(argThat(list -> {
            ReconciliationAnomaly anomaly = list.get(0);
            return anomaly.getAccountDate().equals(accountDate)
                    && anomaly.getInventoryType() == InventoryType.SKU
                    && anomaly.getDiffQty().compareTo(BigDecimal.valueOf(5)) == 0;
        }));
    }

    @Test
    @DisplayName("同账期重复对账 → 跳过")
    void reconcile_duplicate_shouldSkip() {
        when(reconciliationRepository.existsByAccountDate(accountDate)).thenReturn(true);

        int count = service.reconcile(accountDate);

        assertEquals(0, count);
        verifyNoInteractions(reconciliationMapper, inventorySkuRepository, inventoryMaterialRepository);
    }

    @Test
    @DisplayName("无流水但有库存 → 正常检查不报错")
    void reconcile_noOps_shouldStillCheck() {
        when(reconciliationRepository.existsByAccountDate(accountDate)).thenReturn(false);
        when(reconciliationMapper.selectSkuOpsNetChangeByDate(accountDate)).thenReturn(Collections.emptyList());
        when(reconciliationMapper.selectMaterialOpsNetChangeByDate(accountDate)).thenReturn(Collections.emptyList());

        // 库存一致
        InventorySku sku = buildSkuInventory(1L, 50, 10, 0, 60);
        InventoryMaterial material = buildMaterialInventory(1L, BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(100));
        when(inventorySkuRepository.selectAll()).thenReturn(List.of(sku));
        when(inventoryMaterialRepository.selectAll()).thenReturn(List.of(material));

        int count = service.reconcile(accountDate);

        assertEquals(0, count);
    }

    @Test
    @DisplayName("原料库存不一致 → 生成异常")
    void reconcile_materialInconsistent_shouldGenerateAnomaly() {
        when(reconciliationRepository.existsByAccountDate(accountDate)).thenReturn(false);
        when(reconciliationMapper.selectSkuOpsNetChangeByDate(accountDate)).thenReturn(Collections.emptyList());
        // 提供 inventory_id=1 的操作流水，使对账循环能检查到该记录
        when(reconciliationMapper.selectMaterialOpsNetChangeByDate(accountDate))
                .thenReturn(Collections.singletonList(new Object[]{1L, BigDecimal.TEN}));

        InventoryMaterial material = buildMaterialInventory(1L,
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(110));
        when(inventorySkuRepository.selectAll()).thenReturn(Collections.emptyList());
        when(inventoryMaterialRepository.selectAll()).thenReturn(List.of(material));

        int count = service.reconcile(accountDate);

        assertEquals(1, count);
        verify(reconciliationRepository).insertBatch(argThat(list -> {
            ReconciliationAnomaly anomaly = list.get(0);
            return anomaly.getInventoryType() == InventoryType.MATERIAL
                    && anomaly.getDiffQty().compareTo(BigDecimal.TEN) == 0;
        }));
    }

    private InventorySku buildSkuInventory(Long id, int available, int locked, int qc, int total) {
        InventorySku sku = new InventorySku();
        sku.setId(id);
        sku.setSkuId(1L);
        sku.setWarehouseId(1L);
        sku.setAvailableQty(available);
        sku.setLockedQty(locked);
        sku.setQcQty(qc);
        sku.setTotalQty(total);
        return sku;
    }

    private InventoryMaterial buildMaterialInventory(Long id, BigDecimal available,
                                                      BigDecimal locked, BigDecimal qc, BigDecimal total) {
        InventoryMaterial material = new InventoryMaterial();
        material.setId(id);
        material.setMaterialId(1L);
        material.setWarehouseId(1L);
        material.setAvailableQty(available);
        material.setLockedQty(locked);
        material.setQcQty(qc);
        material.setTotalQty(total);
        return material;
    }
}
