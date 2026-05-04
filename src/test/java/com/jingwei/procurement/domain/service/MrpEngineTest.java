package com.jingwei.procurement.domain.service;

import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.order.domain.model.ProductionOrder;
import com.jingwei.order.domain.model.ProductionOrderLine;
import com.jingwei.order.domain.model.SizeMatrix;
import com.jingwei.order.domain.repository.ProductionOrderLineRepository;
import com.jingwei.order.domain.repository.ProductionOrderRepository;
import com.jingwei.procurement.domain.model.*;
import com.jingwei.procurement.domain.repository.BomRepository;
import com.jingwei.procurement.domain.repository.MrpResultRepository;
import com.jingwei.procurement.domain.repository.MrpSourceRepository;
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
 * MRP 计算引擎测试
 * <p>
 * 覆盖 T-26 验收标准：
 * <ul>
 *   <li>FIXED_PER_PIECE 计算 → 需求 = 单件用量 × 总件数</li>
 *   <li>SIZE_DEPENDENT 计算 → 各尺码用量 × 对应数量求和</li>
 *   <li>扣减库存 → 净需求 = 总需求 - 可用库存</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class MrpEngineTest {

    @Mock
    private ProductionOrderRepository productionOrderRepository;
    @Mock
    private ProductionOrderLineRepository productionOrderLineRepository;
    @Mock
    private BomRepository bomRepository;
    @Mock
    private InventoryQueryService inventoryQueryService;
    @Mock
    private MrpResultRepository mrpResultRepository;
    @Mock
    private MrpSourceRepository mrpSourceRepository;
    @Mock
    private CodingRuleDomainService codingRuleDomainService;

    private MrpEngine mrpEngine;

    @BeforeEach
    void setUp() {
        mrpEngine = new MrpEngine(
                productionOrderRepository, productionOrderLineRepository,
                bomRepository, inventoryQueryService,
                mrpResultRepository, mrpSourceRepository, codingRuleDomainService);
    }

    // ==================== 辅助方法 ====================

    private ProductionOrder buildOrder(Long id) {
        ProductionOrder order = new ProductionOrder();
        order.setId(id);
        order.setOrderNo("MO-202605-00001");
        return order;
    }

    private ProductionOrderLine buildLine(Long id, Long orderId, Long bomId, SizeMatrix sizeMatrix) {
        ProductionOrderLine line = new ProductionOrderLine();
        line.setId(id);
        line.setOrderId(orderId);
        line.setSpuId(201L);
        line.setColorWayId(301L);
        line.setBomId(bomId);
        line.setSizeMatrix(sizeMatrix);
        line.setTotalQuantity(sizeMatrix.getTotalQuantity());
        return line;
    }

    private Bom buildBom(Long id, List<BomItem> items) {
        Bom bom = new Bom();
        bom.setId(id);
        bom.setSpuId(201L);
        bom.setBomVersion(1);
        bom.setStatus(BomStatus.APPROVED);
        bom.setItems(items);
        return bom;
    }

    private BomItem buildFixedItem(Long materialId, BigDecimal consumption) {
        BomItem item = new BomItem();
        item.setId(1L);
        item.setBomId(1L);
        item.setMaterialId(materialId);
        item.setMaterialType("TRIM");
        item.setConsumptionType(ConsumptionType.FIXED_PER_PIECE);
        item.setBaseConsumption(consumption);
        item.setUnit("个");
        item.setWastageRate(BigDecimal.ZERO);
        return item;
    }

    private BomItem buildSizeDependentItem(Long materialId, BigDecimal wastageRate) {
        BomItem item = new BomItem();
        item.setId(2L);
        item.setBomId(1L);
        item.setMaterialId(materialId);
        item.setMaterialType("FABRIC");
        item.setConsumptionType(ConsumptionType.SIZE_DEPENDENT);
        item.setBaseConsumption(new BigDecimal("1.80"));
        item.setBaseSizeId(11L);
        item.setUnit("米");
        item.setWastageRate(wastageRate);

        List<SizeConsumptions.SizeConsumptionEntry> entries = List.of(
                new SizeConsumptions.SizeConsumptionEntry(10L, "S", new BigDecimal("1.60")),
                new SizeConsumptions.SizeConsumptionEntry(11L, "M", new BigDecimal("1.80")),
                new SizeConsumptions.SizeConsumptionEntry(12L, "L", new BigDecimal("1.95"))
        );
        item.setSizeConsumptions(new SizeConsumptions(11L, "M", new BigDecimal("1.80"), entries));
        return item;
    }

    private SizeMatrix buildSizeMatrix() {
        return new SizeMatrix(1L, List.of(
                new SizeMatrix.SizeEntry(10L, "S", 100),
                new SizeMatrix.SizeEntry(11L, "M", 200),
                new SizeMatrix.SizeEntry(12L, "L", 300)
        ));
    }

    // ==================== 计算测试 ====================

    @Nested
    @DisplayName("FIXED_PER_PIECE 计算")
    class FixedPerPieceCalculation {

        @Test
        @DisplayName("需求 = 单件用量 × 总件数")
        void shouldCalculateFixedPerPieceCorrectly() {
            SizeMatrix matrix = buildSizeMatrix(); // 600件
            BomItem item = buildFixedItem(1L, new BigDecimal("8")); // 8个/件

            MrpEngine.DemandItem demand = new MrpEngine.DemandItem(
                    1L, 10L, 201L, 301L, 1L, matrix);

            BigDecimal result = mrpEngine.calculateMaterialQuantity(item, demand);

            // 8 × 600 = 4800
            assertEquals(new BigDecimal("4800.00"), result);
        }
    }

    @Nested
    @DisplayName("SIZE_DEPENDENT 计算")
    class SizeDependentCalculation {

        @Test
        @DisplayName("各尺码用量 × 对应数量求和，加损耗率")
        void shouldCalculateSizeDependentCorrectly() {
            SizeMatrix matrix = buildSizeMatrix(); // S:100, M:200, L:300
            BomItem item = buildSizeDependentItem(2L, new BigDecimal("0.08")); // 8%损耗

            MrpEngine.DemandItem demand = new MrpEngine.DemandItem(
                    1L, 10L, 201L, 301L, 1L, matrix);

            BigDecimal result = mrpEngine.calculateMaterialQuantity(item, demand);

            // S: 1.60×100 = 160
            // M: 1.80×200 = 360
            // L: 1.95×300 = 585
            // 小计: 1105
            // 加8%损耗: 1105 × 1.08 = 1193.40
            assertEquals(new BigDecimal("1193.40"), result);
        }
    }

    @Nested
    @DisplayName("库存扣减")
    class InventoryDeduction {

        @Test
        @DisplayName("净需求 = 总需求 - 可用库存")
        void shouldDeductInventoryCorrectly() {
            SizeMatrix matrix = buildSizeMatrix();
            Bom bom = buildBom(1L, List.of(buildFixedItem(1L, new BigDecimal("8"))));

            when(codingRuleDomainService.generateCode(any(), any())).thenReturn("MRP-20260504-00001");
            when(productionOrderRepository.selectById(1L)).thenReturn(buildOrder(1L));
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(
                    buildLine(10L, 1L, 1L, matrix)
            ));
            when(bomRepository.selectDetailById(1L)).thenReturn(bom);
            // 可用库存2000，在途0
            when(inventoryQueryService.getAvailableStock(1L)).thenReturn(new BigDecimal("2000"));
            when(inventoryQueryService.getInTransitQuantity(1L)).thenReturn(BigDecimal.ZERO);
            when(mrpResultRepository.insert(any())).thenReturn(1);
            when(mrpSourceRepository.insert(any())).thenReturn(1);

            MrpCalculateResult calcResult = mrpEngine.calculate(List.of(1L));
            List<MrpResult> results = calcResult.getResults();

            assertEquals(1, results.size());
            MrpResult result = results.get(0);
            // 毛需求: 8×600 = 4800
            assertEquals(new BigDecimal("4800.00"), result.getGrossDemand());
            // 可用库存: 2000
            assertEquals(new BigDecimal("2000"), result.getAllocatedStock());
            // 净需求: 4800 - 2000 = 2800
            assertEquals(new BigDecimal("2800.00"), result.getNetDemand());
            // 建议采购量 = 净需求
            assertEquals(new BigDecimal("2800.00"), result.getSuggestedQuantity());
        }

        @Test
        @DisplayName("库存充足时净需求为0")
        void shouldReturnZeroNetDemandWhenStockSufficient() {
            SizeMatrix matrix = buildSizeMatrix();
            Bom bom = buildBom(1L, List.of(buildFixedItem(1L, new BigDecimal("1")))); // 1个/件

            when(codingRuleDomainService.generateCode(any(), any())).thenReturn("MRP-20260504-00001");
            when(productionOrderRepository.selectById(1L)).thenReturn(buildOrder(1L));
            when(productionOrderLineRepository.selectByOrderId(1L)).thenReturn(List.of(
                    buildLine(10L, 1L, 1L, matrix)
            ));
            when(bomRepository.selectDetailById(1L)).thenReturn(bom);
            when(inventoryQueryService.getAvailableStock(1L)).thenReturn(new BigDecimal("1000"));
            when(inventoryQueryService.getInTransitQuantity(1L)).thenReturn(BigDecimal.ZERO);
            when(mrpResultRepository.insert(any())).thenReturn(1);
            when(mrpSourceRepository.insert(any())).thenReturn(1);

            MrpCalculateResult calcResult = mrpEngine.calculate(List.of(1L));
            List<MrpResult> results = calcResult.getResults();

            MrpResult result = results.get(0);
            // 毛需求: 1×600 = 600, 库存1000 → 净需求=0
            assertEquals(BigDecimal.ZERO.compareTo(result.getNetDemand()), 0);
        }
    }
}
