package com.jingwei.master;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.ColorWay;
import com.jingwei.master.domain.model.Spu;
import com.jingwei.master.domain.model.SpuStatus;
import com.jingwei.master.domain.repository.ColorWayRepository;
import com.jingwei.master.domain.repository.SizeGroupRepository;
import com.jingwei.master.domain.repository.SizeRepository;
import com.jingwei.master.domain.repository.SkuRepository;
import com.jingwei.master.domain.repository.SpuRepository;
import com.jingwei.master.domain.service.SpuDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SpuDomainService 批量价格更新 单元测试
 * <p>
 * 测试批量更新 SKU 价格的核心业务规则：
 * <ul>
 *   <li>按 SPU 维度批量更新：更新该款式下所有 SKU</li>
 *   <li>按颜色款维度批量更新：仅更新指定颜色下所有 SKU</li>
 *   <li>校验：至少传入一个价格字段、SPU 存在性、颜色款存在性、颜色款归属</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class SpuDomainServiceBatchPriceTest {

    @Mock
    private SpuRepository spuRepository;

    @Mock
    private ColorWayRepository colorWayRepository;

    @Mock
    private SkuRepository skuRepository;

    @Mock
    private SizeGroupRepository sizeGroupRepository;

    @Mock
    private SizeRepository sizeRepository;

    @InjectMocks
    private SpuDomainService spuDomainService;

    // ==================== 按 SPU 维度批量更新 ====================

    @Test
    @DisplayName("按SPU维度批量更新 — 只传销售价，应只调用 batchUpdatePrice 一次")
    void batchUpdateSkuPrice_bySpu_singlePriceField_shouldCallOnce() {
        Spu spu = buildSpu(1L, "SP20260001");
        when(spuRepository.selectById(1L)).thenReturn(spu);
        when(skuRepository.batchUpdatePrice(1L, "salePrice", new BigDecimal("199.00"))).thenReturn(3);

        int rows = spuDomainService.batchUpdateSkuPrice(
                1L, null, null, new BigDecimal("199.00"), null);

        assertEquals(3, rows);
        verify(skuRepository).batchUpdatePrice(1L, "salePrice", new BigDecimal("199.00"));
        // 不应调用按颜色款维度的方法
        verify(skuRepository, never()).batchUpdatePriceByColorWay(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("按SPU维度批量更新 — 传三种价格，应调用三次 batchUpdatePrice")
    void batchUpdateSkuPrice_bySpu_allPriceFields_shouldCallThreeTimes() {
        Spu spu = buildSpu(1L, "SP20260001");
        when(spuRepository.selectById(1L)).thenReturn(spu);
        when(skuRepository.batchUpdatePrice(eq(1L), anyString(), any(BigDecimal.class))).thenReturn(3);

        int rows = spuDomainService.batchUpdateSkuPrice(
                1L, null,
                new BigDecimal("80.00"), new BigDecimal("199.00"), new BigDecimal("150.00"));

        assertEquals(9, rows); // 3 种价格 × 3 行 = 9
        verify(skuRepository, times(3)).batchUpdatePrice(eq(1L), anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("按SPU维度批量更新 — 传两种价格，应调用两次")
    void batchUpdateSkuPrice_bySpu_twoPriceFields_shouldCallTwice() {
        Spu spu = buildSpu(1L, "SP20260001");
        when(spuRepository.selectById(1L)).thenReturn(spu);
        when(skuRepository.batchUpdatePrice(eq(1L), anyString(), any(BigDecimal.class))).thenReturn(5);

        int rows = spuDomainService.batchUpdateSkuPrice(
                1L, null,
                new BigDecimal("80.00"), new BigDecimal("199.00"), null);

        assertEquals(10, rows); // 2 种价格 × 5 行 = 10
        verify(skuRepository, times(2)).batchUpdatePrice(eq(1L), anyString(), any(BigDecimal.class));
    }

    // ==================== 按颜色款维度批量更新 ====================

    @Test
    @DisplayName("按颜色款维度批量更新 — 只更新指定颜色的 SKU")
    void batchUpdateSkuPrice_byColorWay_shouldCallColorWayMethod() {
        Spu spu = buildSpu(1L, "SP20260001");
        ColorWay colorWay = buildColorWay(10L, 1L, "黑色", "BK");

        when(spuRepository.selectById(1L)).thenReturn(spu);
        when(colorWayRepository.selectById(10L)).thenReturn(colorWay);
        when(skuRepository.batchUpdatePriceByColorWay(10L, "salePrice", new BigDecimal("199.00")))
                .thenReturn(4);

        int rows = spuDomainService.batchUpdateSkuPrice(
                1L, 10L, null, new BigDecimal("199.00"), null);

        assertEquals(4, rows);
        verify(skuRepository).batchUpdatePriceByColorWay(10L, "salePrice", new BigDecimal("199.00"));
        // 不应调用按 SPU 维度的方法
        verify(skuRepository, never()).batchUpdatePrice(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("按颜色款维度批量更新 — 传三种价格，应调用三次 batchUpdatePriceByColorWay")
    void batchUpdateSkuPrice_byColorWay_allPriceFields_shouldCallThreeTimes() {
        Spu spu = buildSpu(1L, "SP20260001");
        ColorWay colorWay = buildColorWay(10L, 1L, "黑色", "BK");

        when(spuRepository.selectById(1L)).thenReturn(spu);
        when(colorWayRepository.selectById(10L)).thenReturn(colorWay);
        when(skuRepository.batchUpdatePriceByColorWay(eq(10L), anyString(), any(BigDecimal.class)))
                .thenReturn(4);

        int rows = spuDomainService.batchUpdateSkuPrice(
                1L, 10L,
                new BigDecimal("80.00"), new BigDecimal("199.00"), new BigDecimal("150.00"));

        assertEquals(12, rows); // 3 种价格 × 4 行 = 12
        verify(skuRepository, times(3)).batchUpdatePriceByColorWay(eq(10L), anyString(), any(BigDecimal.class));
    }

    // ==================== 校验：至少传入一个价格 ====================

    @Test
    @DisplayName("批量更新 — 所有价格字段为空应抛异常")
    void batchUpdateSkuPrice_noPriceFields_shouldThrow() {
        BizException ex = assertThrows(BizException.class,
                () -> spuDomainService.batchUpdateSkuPrice(1L, null, null, null, null));
        assertEquals(ErrorCode.PARAM_VALIDATION_FAILED.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("至少传入一个价格字段"));
    }

    // ==================== 校验：SPU 存在性 ====================

    @Test
    @DisplayName("批量更新 — SPU 不存在应抛异常")
    void batchUpdateSkuPrice_spuNotFound_shouldThrow() {
        when(spuRepository.selectById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> spuDomainService.batchUpdateSkuPrice(
                        999L, null, new BigDecimal("100.00"), null, null));
        assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("款式不存在"));
    }

    // ==================== 校验：颜色款存在性 ====================

    @Test
    @DisplayName("批量更新 — 颜色款不存在应抛异常")
    void batchUpdateSkuPrice_colorWayNotFound_shouldThrow() {
        Spu spu = buildSpu(1L, "SP20260001");
        when(spuRepository.selectById(1L)).thenReturn(spu);
        when(colorWayRepository.selectById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> spuDomainService.batchUpdateSkuPrice(
                        1L, 999L, new BigDecimal("100.00"), null, null));
        assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("颜色款不存在"));
    }

    // ==================== 校验：颜色款归属 ====================

    @Test
    @DisplayName("批量更新 — 颜色款不属于该SPU应抛异常")
    void batchUpdateSkuPrice_colorWayNotBelongToSpu_shouldThrow() {
        Spu spu = buildSpu(1L, "SP20260001");
        // 颜色款属于 SPU 2，但传入的 spuId 是 1
        ColorWay colorWay = buildColorWay(10L, 2L, "黑色", "BK");

        when(spuRepository.selectById(1L)).thenReturn(spu);
        when(colorWayRepository.selectById(10L)).thenReturn(colorWay);

        BizException ex = assertThrows(BizException.class,
                () -> spuDomainService.batchUpdateSkuPrice(
                        1L, 10L, new BigDecimal("100.00"), null, null));
        assertEquals(ErrorCode.PARAM_VALIDATION_FAILED.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("颜色款不属于该款式"));
    }

    // ==================== 辅助方法 ====================

    private Spu buildSpu(Long id, String code) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setCode(code);
        spu.setName("测试款式");
        spu.setStatus(SpuStatus.ACTIVE);
        return spu;
    }

    private ColorWay buildColorWay(Long id, Long spuId, String colorName, String colorCode) {
        ColorWay cw = new ColorWay();
        cw.setId(id);
        cw.setSpuId(spuId);
        cw.setColorName(colorName);
        cw.setColorCode(colorCode);
        cw.setSortOrder(0);
        return cw;
    }
}
