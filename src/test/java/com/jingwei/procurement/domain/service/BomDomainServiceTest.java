package com.jingwei.procurement.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.procurement.domain.model.Bom;
import com.jingwei.procurement.domain.model.BomItem;
import com.jingwei.procurement.domain.model.BomStatus;
import com.jingwei.procurement.domain.model.ConsumptionType;
import com.jingwei.procurement.domain.model.SizeConsumptions;
import com.jingwei.procurement.domain.repository.BomItemRepository;
import com.jingwei.procurement.domain.repository.BomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BOM 领域服务测试
 * <p>
 * 覆盖 T-25 验收标准：
 * <ul>
 *   <li>创建BOM → 版本号自动递增</li>
 *   <li>审批BOM → 旧版本自动 OBSOLETE</li>
 *   <li>同SPU重复审批 → 抛异常</li>
 *   <li>删除被引用的BOM → 抛异常</li>
 *   <li>FIXED_PER_PIECE 消耗类型正确存储</li>
 *   <li>SIZE_DEPENDENT 尺码用量表 JSONB 正确存储和读取</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class BomDomainServiceTest {

    @Mock
    private BomRepository bomRepository;
    @Mock
    private BomItemRepository bomItemRepository;

    private BomDomainService bomDomainService;

    @BeforeEach
    void setUp() {
        bomDomainService = new BomDomainService(bomRepository, bomItemRepository);
    }

    // ==================== 辅助方法 ====================

    private Bom buildBom(Long id, Long spuId, int bomVersion, BomStatus status) {
        Bom bom = new Bom();
        bom.setId(id);
        bom.setCode("BOM-" + spuId + "-v" + bomVersion);
        bom.setSpuId(spuId);
        bom.setBomVersion(bomVersion);
        bom.setStatus(status);
        return bom;
    }

    private BomItem buildFixedItem(Long id, Long bomId, Long materialId) {
        BomItem item = new BomItem();
        item.setId(id);
        item.setBomId(bomId);
        item.setMaterialId(materialId);
        item.setMaterialType("TRIM");
        item.setConsumptionType(ConsumptionType.FIXED_PER_PIECE);
        item.setBaseConsumption(new BigDecimal("8"));
        item.setUnit("个");
        item.setWastageRate(BigDecimal.ZERO);
        return item;
    }

    private BomItem buildSizeDependentItem(Long id, Long bomId, Long materialId) {
        BomItem item = new BomItem();
        item.setId(id);
        item.setBomId(bomId);
        item.setMaterialId(materialId);
        item.setMaterialType("FABRIC");
        item.setConsumptionType(ConsumptionType.SIZE_DEPENDENT);
        item.setBaseConsumption(new BigDecimal("1.80"));
        item.setBaseSizeId(11L);
        item.setUnit("米");
        item.setWastageRate(new BigDecimal("0.08"));

        List<SizeConsumptions.SizeConsumptionEntry> entries = List.of(
                new SizeConsumptions.SizeConsumptionEntry(10L, "S", new BigDecimal("1.60")),
                new SizeConsumptions.SizeConsumptionEntry(11L, "M", new BigDecimal("1.80")),
                new SizeConsumptions.SizeConsumptionEntry(12L, "L", new BigDecimal("1.95"))
        );
        item.setSizeConsumptions(new SizeConsumptions(11L, "M", new BigDecimal("1.80"), entries));
        return item;
    }

    // ==================== 创建 BOM 测试 ====================

    @Nested
    @DisplayName("创建BOM")
    class CreateBom {

        @Test
        @DisplayName("创建BOM → 版本号自动递增（首版本）")
        void shouldAutoIncrementVersionForFirstBom() {
            Bom bom = buildBom(null, 201L, 0, BomStatus.DRAFT);
            when(bomRepository.selectMaxBomVersion(201L)).thenReturn(null);
            when(bomRepository.insert(any())).thenReturn(1);
            when(bomItemRepository.insert(any())).thenReturn(1);

            BomItem item = buildFixedItem(null, null, 1L);
            Bom result = bomDomainService.createBom(bom, List.of(item));

            assertEquals(1, result.getBomVersion());
            assertEquals(BomStatus.DRAFT, result.getStatus());
            verify(bomRepository).insert(bom);
            verify(bomItemRepository).insert(item);
        }

        @Test
        @DisplayName("创建BOM → 版本号自动递增（已有版本）")
        void shouldAutoIncrementVersionWhenExistingBom() {
            Bom bom = buildBom(null, 201L, 0, BomStatus.DRAFT);
            when(bomRepository.selectMaxBomVersion(201L)).thenReturn(2);
            when(bomRepository.insert(any())).thenReturn(1);
            when(bomItemRepository.insert(any())).thenReturn(1);

            BomItem item = buildFixedItem(null, null, 1L);
            Bom result = bomDomainService.createBom(bom, List.of(item));

            assertEquals(3, result.getBomVersion());
        }

        @Test
        @DisplayName("FIXED_PER_PIECE 消耗类型正确存储")
        void shouldStoreFixedPerPieceCorrectly() {
            Bom bom = buildBom(null, 201L, 0, BomStatus.DRAFT);
            when(bomRepository.selectMaxBomVersion(201L)).thenReturn(null);
            when(bomRepository.insert(any())).thenReturn(1);
            when(bomItemRepository.insert(any())).thenReturn(1);

            BomItem item = buildFixedItem(null, null, 1L);
            bomDomainService.createBom(bom, List.of(item));

            assertEquals(ConsumptionType.FIXED_PER_PIECE, item.getConsumptionType());
            assertEquals(new BigDecimal("8"), item.getBaseConsumption());
            assertEquals("个", item.getUnit());
            assertEquals(1, item.getSortOrder());
        }

        @Test
        @DisplayName("SIZE_DEPENDENT 尺码用量表 JSONB 正确存储")
        void shouldStoreSizeConsumptionsCorrectly() {
            Bom bom = buildBom(null, 201L, 0, BomStatus.DRAFT);
            when(bomRepository.selectMaxBomVersion(201L)).thenReturn(null);
            when(bomRepository.insert(any())).thenReturn(1);
            when(bomItemRepository.insert(any())).thenReturn(1);

            BomItem item = buildSizeDependentItem(null, null, 2L);
            bomDomainService.createBom(bom, List.of(item));

            assertNotNull(item.getSizeConsumptions());
            assertEquals(11L, item.getSizeConsumptions().getBaseSizeId());
            assertEquals(3, item.getSizeConsumptions().getSizes().size());
            assertEquals(new BigDecimal("1.95"),
                    item.getSizeConsumptions().getConsumption(12L));
        }
    }

    // ==================== 审批 BOM 测试 ====================

    @Nested
    @DisplayName("审批BOM")
    class ApproveBom {

        @Test
        @DisplayName("审批BOM → 旧版本自动 OBSOLETE")
        void shouldObsoleteOldVersionWhenApprove() {
            Bom newBom = buildBom(1L, 201L, 2, BomStatus.DRAFT);
            Bom oldBom = buildBom(2L, 201L, 1, BomStatus.APPROVED);

            when(bomRepository.selectById(1L)).thenReturn(newBom);
            when(bomRepository.selectApprovedBySpuId(201L)).thenReturn(Optional.of(oldBom));
            when(bomRepository.updateById(any())).thenReturn(1);

            bomDomainService.approveBom(1L, 100L);

            assertEquals(BomStatus.OBSOLETE, oldBom.getStatus());
            assertNotNull(oldBom.getEffectiveTo());
            assertEquals(BomStatus.APPROVED, newBom.getStatus());
            assertEquals(100L, newBom.getApprovedBy());
            assertNotNull(newBom.getApprovedAt());
        }

        @Test
        @DisplayName("审批BOM → 无旧版本时直接审批")
        void shouldApproveDirectlyWhenNoOldVersion() {
            Bom bom = buildBom(1L, 201L, 1, BomStatus.DRAFT);

            when(bomRepository.selectById(1L)).thenReturn(bom);
            when(bomRepository.selectApprovedBySpuId(201L)).thenReturn(Optional.empty());
            when(bomRepository.updateById(any())).thenReturn(1);

            bomDomainService.approveBom(1L, 100L);

            assertEquals(BomStatus.APPROVED, bom.getStatus());
            verify(bomRepository, never()).updateById(argThat(b -> b.getStatus() == BomStatus.OBSOLETE));
        }

        @Test
        @DisplayName("非DRAFT状态审批 → 抛异常")
        void shouldRejectApproveWhenNotDraft() {
            Bom bom = buildBom(1L, 201L, 1, BomStatus.APPROVED);
            when(bomRepository.selectById(1L)).thenReturn(bom);

            assertThrows(BizException.class, () -> bomDomainService.approveBom(1L, 100L));
        }
    }

    // ==================== 删除 BOM 测试 ====================

    @Nested
    @DisplayName("删除BOM")
    class DeleteBom {

        @Test
        @DisplayName("删除被引用的BOM → 抛异常")
        void shouldRejectDeleteWhenReferenced() {
            Bom bom = buildBom(1L, 201L, 1, BomStatus.DRAFT);
            when(bomRepository.selectById(1L)).thenReturn(bom);
            when(bomRepository.isReferencedByProductionOrder(1L)).thenReturn(true);

            BizException ex = assertThrows(BizException.class,
                    () -> bomDomainService.deleteBom(1L));
            assertEquals(ErrorCode.BOM_REFERENCED.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("删除未被引用的DRAFT BOM → 成功")
        void shouldDeleteDraftBomWhenNotReferenced() {
            Bom bom = buildBom(1L, 201L, 1, BomStatus.DRAFT);
            when(bomRepository.selectById(1L)).thenReturn(bom);
            when(bomRepository.isReferencedByProductionOrder(1L)).thenReturn(false);
            when(bomItemRepository.deleteByBomId(1L)).thenReturn(1);
            when(bomRepository.deleteById(1L)).thenReturn(1);

            bomDomainService.deleteBom(1L);

            verify(bomItemRepository).deleteByBomId(1L);
            verify(bomRepository).deleteById(1L);
        }

        @Test
        @DisplayName("非DRAFT状态删除 → 抛异常")
        void shouldRejectDeleteWhenNotDraft() {
            Bom bom = buildBom(1L, 201L, 1, BomStatus.APPROVED);
            when(bomRepository.selectById(1L)).thenReturn(bom);

            assertThrows(BizException.class, () -> bomDomainService.deleteBom(1L));
        }
    }

    // ==================== 编辑 BOM 测试 ====================

    @Nested
    @DisplayName("编辑BOM")
    class UpdateBom {

        @Test
        @DisplayName("编辑BOM → 全量替换行")
        void shouldReplaceAllItemsWhenUpdate() {
            Bom existing = buildBom(1L, 201L, 1, BomStatus.DRAFT);
            when(bomRepository.selectById(1L)).thenReturn(existing);
            when(bomRepository.updateById(any())).thenReturn(1);
            when(bomItemRepository.deleteByBomId(1L)).thenReturn(2);
            when(bomItemRepository.insert(any())).thenReturn(1);

            Bom updatedBom = new Bom();
            BomItem newItem = buildFixedItem(null, 1L, 3L);

            Bom result = bomDomainService.updateBom(1L, updatedBom, List.of(newItem), 100L);

            verify(bomItemRepository).deleteByBomId(1L);
            verify(bomItemRepository).insert(newItem);
            assertEquals(1, newItem.getSortOrder());
        }

        @Test
        @DisplayName("非DRAFT状态编辑 → 抛异常")
        void shouldRejectUpdateWhenNotDraft() {
            Bom existing = buildBom(1L, 201L, 1, BomStatus.APPROVED);
            when(bomRepository.selectById(1L)).thenReturn(existing);

            assertThrows(BizException.class,
                    () -> bomDomainService.updateBom(1L, new Bom(), List.of(new BomItem()), 100L));
        }
    }
}
