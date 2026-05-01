package com.jingwei.master;

import com.jingwei.master.application.dto.BatchUpdateSkuPriceDTO;
import com.jingwei.master.application.service.SpuApplicationService;
import com.jingwei.master.domain.service.SpuDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SpuApplicationService 批量价格更新 单元测试
 * <p>
 * 测试应用服务层的编排逻辑：
 * <ul>
 *   <li>DTO 字段正确透传到 DomainService</li>
 *   <li>按 SPU 维度：colorWayId 为空时正确透传</li>
 *   <li>按颜色款维度：colorWayId 非空时正确透传</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class SpuApplicationServiceBatchPriceTest {

    @Mock
    private SpuDomainService spuDomainService;

    @InjectMocks
    private SpuApplicationService spuApplicationService;

    @Test
    @DisplayName("批量更新 — DTO字段正确透传到DomainService（按SPU维度）")
    void batchUpdateSkuPrice_bySpu_shouldPassFieldsCorrectly() {
        BatchUpdateSkuPriceDTO dto = new BatchUpdateSkuPriceDTO();
        dto.setSpuId(1L);
        dto.setColorWayId(null);
        dto.setCostPrice(new BigDecimal("80.00"));
        dto.setSalePrice(new BigDecimal("199.00"));
        dto.setWholesalePrice(new BigDecimal("150.00"));

        when(spuDomainService.batchUpdateSkuPrice(
                1L, null, new BigDecimal("80.00"), new BigDecimal("199.00"), new BigDecimal("150.00")))
                .thenReturn(9);

        int rows = spuApplicationService.batchUpdateSkuPrice(dto);

        assertEquals(9, rows);
        verify(spuDomainService).batchUpdateSkuPrice(
                1L, null, new BigDecimal("80.00"), new BigDecimal("199.00"), new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("批量更新 — DTO字段正确透传到DomainService（按颜色款维度）")
    void batchUpdateSkuPrice_byColorWay_shouldPassFieldsCorrectly() {
        BatchUpdateSkuPriceDTO dto = new BatchUpdateSkuPriceDTO();
        dto.setSpuId(1L);
        dto.setColorWayId(10L);
        dto.setSalePrice(new BigDecimal("199.00"));

        when(spuDomainService.batchUpdateSkuPrice(
                1L, 10L, null, new BigDecimal("199.00"), null))
                .thenReturn(4);

        int rows = spuApplicationService.batchUpdateSkuPrice(dto);

        assertEquals(4, rows);
        verify(spuDomainService).batchUpdateSkuPrice(
                1L, 10L, null, new BigDecimal("199.00"), null);
    }

    @Test
    @DisplayName("批量更新 — 只传部分价格字段时其余为null")
    void batchUpdateSkuPrice_partialPriceFields_shouldPassNulls() {
        BatchUpdateSkuPriceDTO dto = new BatchUpdateSkuPriceDTO();
        dto.setSpuId(2L);
        dto.setColorWayId(null);
        dto.setCostPrice(new BigDecimal("50.00"));

        when(spuDomainService.batchUpdateSkuPrice(
                2L, null, new BigDecimal("50.00"), null, null))
                .thenReturn(3);

        int rows = spuApplicationService.batchUpdateSkuPrice(dto);

        assertEquals(3, rows);
        verify(spuDomainService).batchUpdateSkuPrice(
                2L, null, new BigDecimal("50.00"), null, null);
    }

    @Test
    @DisplayName("批量更新 — DomainService抛异常时正确传播")
    void batchUpdateSkuPrice_domainServiceThrows_shouldPropagate() {
        BatchUpdateSkuPriceDTO dto = new BatchUpdateSkuPriceDTO();
        dto.setSpuId(999L);
        dto.setSalePrice(new BigDecimal("199.00"));

        when(spuDomainService.batchUpdateSkuPrice(
                999L, null, null, new BigDecimal("199.00"), null))
                .thenThrow(new com.jingwei.common.domain.model.BizException(
                        com.jingwei.common.domain.model.ErrorCode.DATA_NOT_FOUND, "款式不存在"));

        assertThrows(com.jingwei.common.domain.model.BizException.class,
                () -> spuApplicationService.batchUpdateSkuPrice(dto));
    }
}
