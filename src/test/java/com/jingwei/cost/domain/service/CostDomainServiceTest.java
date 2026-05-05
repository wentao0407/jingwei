package com.jingwei.cost.domain.service;

import com.jingwei.cost.domain.model.CostMaterialIssue;
import com.jingwei.cost.domain.model.CostProductionOrder;
import com.jingwei.cost.domain.model.MaterialType;
import com.jingwei.cost.domain.repository.CostMaterialIssueRepository;
import com.jingwei.cost.domain.repository.CostProductionOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 成本核算领域服务单元测试
 * <p>
 * 覆盖 T-39 验收标准的核心功能：
 * <ul>
 *   <li>领料成本 = 领料数量 × 物料单位成本</li>
 *   <li>加权平均 = (原库存金额 + 入库金额) / (原数量 + 入库数量)</li>
 *   <li>生产订单成本归集按物料类型分类</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class CostDomainServiceTest {

    @Mock
    private CostProductionOrderRepository costProductionOrderRepository;
    @Mock
    private CostMaterialIssueRepository costMaterialIssueRepository;

    private CostDomainService service;

    @BeforeEach
    void setUp() {
        service = new CostDomainService(costProductionOrderRepository, costMaterialIssueRepository);
    }

    // ==================== 领料成本记录 ====================

    @Nested
    @DisplayName("领料成本记录")
    class RecordMaterialIssueTests {

        @Test
        @DisplayName("首次领料 → 创建成本归集记录 + 领料记录")
        void record_firstIssue_shouldCreateCostRecord() {
            // 无已有成本记录
            when(costProductionOrderRepository.selectByOrderLineId(1L, 10L)).thenReturn(null);

            CostMaterialIssue result = service.recordMaterialIssue(
                    1L, 10L, 100L, MaterialType.MATERIAL,
                    BigDecimal.valueOf(100), BigDecimal.valueOf(48), null);

            // 验证领料成本 = 100 × 48 = 4800
            assertEquals(0, BigDecimal.valueOf(4800).compareTo(result.getCostAmount()));

            // 验证创建了成本归集记录
            verify(costProductionOrderRepository).insert(argThat(cost ->
                    cost.getMaterialCost().compareTo(BigDecimal.valueOf(4800)) == 0
                            && cost.getTotalCost().compareTo(BigDecimal.valueOf(4800)) == 0));

            // 验证创建了领料记录
            verify(costMaterialIssueRepository).insert(any(CostMaterialIssue.class));
        }

        @Test
        @DisplayName("累加领料 → 更新已有成本归集记录")
        void record_additionalIssue_shouldUpdateCostRecord() {
            CostProductionOrder existingCost = new CostProductionOrder();
            existingCost.setId(1L);
            existingCost.setProductionOrderId(1L);
            existingCost.setProductionLineId(10L);
            existingCost.setMaterialCost(BigDecimal.valueOf(4800));
            existingCost.setTrimCost(BigDecimal.ZERO);
            existingCost.setPackagingCost(BigDecimal.ZERO);
            existingCost.setTotalCost(BigDecimal.valueOf(4800));

            when(costProductionOrderRepository.selectByOrderLineId(1L, 10L)).thenReturn(existingCost);

            service.recordMaterialIssue(
                    1L, 10L, 101L, MaterialType.TRIM,
                    BigDecimal.valueOf(50), BigDecimal.valueOf(12), null);

            // 验证辅料成本累加
            verify(costProductionOrderRepository).updateById(argThat(cost ->
                    cost.getTrimCost().compareTo(BigDecimal.valueOf(600)) == 0
                            && cost.getTotalCost().compareTo(BigDecimal.valueOf(5400)) == 0));
        }

        @Test
        @DisplayName("包材领料 → 归集到 packagingCost")
        void record_packaging_shouldAccumulateToPackagingCost() {
            when(costProductionOrderRepository.selectByOrderLineId(1L, 10L)).thenReturn(null);

            service.recordMaterialIssue(
                    1L, 10L, 102L, MaterialType.PACKAGING,
                    BigDecimal.valueOf(900), BigDecimal.valueOf(2.5), null);

            verify(costProductionOrderRepository).insert(argThat(cost ->
                    cost.getPackagingCost().compareTo(BigDecimal.valueOf(2250)) == 0));
        }
    }

    // ==================== 成品单位成本计算 ====================

    @Nested
    @DisplayName("成品单位成本计算")
    class CalculateUnitCostTests {

        @Test
        @DisplayName("正常计算 → 总成本 / 完工数量")
        void calculate_shouldDivideTotalCostByCompletedQty() {
            CostProductionOrder costRecord = new CostProductionOrder();
            costRecord.setId(1L);
            costRecord.setProductionOrderId(1L);
            costRecord.setProductionLineId(10L);
            costRecord.setTotalCost(BigDecimal.valueOf(134581.50));
            costRecord.setCompletedQty(0);
            costRecord.setUnitCost(BigDecimal.ZERO);

            when(costProductionOrderRepository.selectByOrderLineId(1L, 10L)).thenReturn(costRecord);

            BigDecimal unitCost = service.calculateUnitCost(1L, 10L, 900);

            // 134581.50 / 900 = 149.54
            assertEquals(0, BigDecimal.valueOf(149.54).compareTo(unitCost));

            verify(costProductionOrderRepository).updateById(argThat(cost ->
                    cost.getCompletedQty() == 900
                            && cost.getUnitCost().compareTo(BigDecimal.valueOf(149.54)) == 0));
        }

        @Test
        @DisplayName("完工数量为0 → 返回0")
        void calculate_zeroQty_shouldReturnZero() {
            BigDecimal unitCost = service.calculateUnitCost(1L, 10L, 0);
            assertEquals(0, BigDecimal.ZERO.compareTo(unitCost));
        }

        @Test
        @DisplayName("成本记录不存在 → 返回0")
        void calculate_noCostRecord_shouldReturnZero() {
            when(costProductionOrderRepository.selectByOrderLineId(1L, 10L)).thenReturn(null);

            BigDecimal unitCost = service.calculateUnitCost(1L, 10L, 900);
            assertEquals(0, BigDecimal.ZERO.compareTo(unitCost));
        }
    }

    // ==================== 加权平均法 ====================

    @Nested
    @DisplayName("加权平均法")
    class WeightedAverageCostTests {

        @Test
        @DisplayName("标准加权平均 → (原库存金额 + 入库金额) / (原数量 + 入库数量)")
        void weightedAverage_shouldCalculateCorrectly() {
            // 原库存：100件 × 50元 = 5000元
            // 入库：200件 × 55元 = 11000元
            // 新成本 = (5000 + 11000) / (100 + 200) = 53.33元
            BigDecimal result = service.calculateWeightedAverageCost(
                    100, BigDecimal.valueOf(50),
                    200, BigDecimal.valueOf(55));

            assertEquals(0, BigDecimal.valueOf(53.33).compareTo(result));
        }

        @Test
        @DisplayName("零库存入库 → 使用入库成本")
        void weightedAverage_zeroOriginal_shouldUseInboundCost() {
            BigDecimal result = service.calculateWeightedAverageCost(
                    0, BigDecimal.ZERO,
                    200, BigDecimal.valueOf(55));

            assertEquals(0, BigDecimal.valueOf(55).compareTo(result));
        }

        @Test
        @DisplayName("零入库数量 → 返回原成本")
        void weightedAverage_zeroInbound_shouldReturnOriginalCost() {
            BigDecimal result = service.calculateWeightedAverageCost(
                    100, BigDecimal.valueOf(50),
                    0, BigDecimal.ZERO);

            // (100*50 + 0) / (100+0) = 50
            assertEquals(0, BigDecimal.valueOf(50).compareTo(result));
        }

        @Test
        @DisplayName("总数量为0 → 返回0")
        void weightedAverage_zeroTotal_shouldReturnZero() {
            BigDecimal result = service.calculateWeightedAverageCost(
                    0, BigDecimal.ZERO,
                    0, BigDecimal.ZERO);

            assertEquals(0, BigDecimal.ZERO.compareTo(result));
        }
    }
}
