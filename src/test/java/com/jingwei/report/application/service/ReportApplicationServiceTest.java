package com.jingwei.report.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.report.application.dto.InventoryAgeQueryDTO;
import com.jingwei.report.application.dto.InventoryLedgerQueryDTO;
import com.jingwei.report.application.dto.OperationFlowQueryDTO;
import com.jingwei.report.application.dto.TurnoverQueryDTO;
import com.jingwei.report.infrastructure.persistence.ReportMapper;
import com.jingwei.report.interfaces.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 报表应用服务单元测试
 * <p>
 * 覆盖 T-42 验收标准：
 * <ul>
 *   <li>库存台账查询（成品/原料）</li>
 *   <li>矩阵视图（颜色 × 尺码）</li>
 *   <li>出入库流水查询</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class ReportApplicationServiceTest {

    @Mock
    private ReportMapper reportMapper;

    @InjectMocks
    private ReportApplicationService service;

    // ==================== 库存台账查询 ====================

    @Test
    @DisplayName("成品库存台账分页查询")
    void queryLedger_sku_shouldReturnPage() {
        InventoryLedgerQueryDTO dto = new InventoryLedgerQueryDTO();
        dto.setInventoryType("SKU");
        dto.setCurrent(1L);
        dto.setSize(20L);

        Page<Map<String, Object>> page = new Page<>(1, 20);
        Map<String, Object> row = buildSkuLedgerRow();
        page.setRecords(List.of(row));
        page.setTotal(1);

        when(reportMapper.selectSkuLedgerPage(any(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(page);

        IPage<InventoryLedgerVO> result = service.queryLedger(dto);

        assertEquals(1, result.getTotal());
        InventoryLedgerVO vo = result.getRecords().get(0);
        assertEquals("SKU", vo.getInventoryType());
        assertEquals("SP20260001-BK-M", vo.getSkuCode());
        assertEquals("黑色", vo.getColorName());
        assertEquals("M", vo.getSizeCode());
    }

    @Test
    @DisplayName("原料库存台账分页查询")
    void queryLedger_material_shouldReturnPage() {
        InventoryLedgerQueryDTO dto = new InventoryLedgerQueryDTO();
        dto.setInventoryType("MATERIAL");
        dto.setCurrent(1L);
        dto.setSize(20L);

        Page<Map<String, Object>> page = new Page<>(1, 20);
        Map<String, Object> row = buildMaterialLedgerRow();
        page.setRecords(List.of(row));
        page.setTotal(1);

        when(reportMapper.selectMaterialLedgerPage(any(), isNull(), isNull(), isNull()))
                .thenReturn(page);

        IPage<InventoryLedgerVO> result = service.queryLedger(dto);

        assertEquals(1, result.getTotal());
        InventoryLedgerVO vo = result.getRecords().get(0);
        assertEquals("MATERIAL", vo.getInventoryType());
        assertEquals("MAT001", vo.getMaterialCode());
    }

    // ==================== 矩阵视图 ====================

    @Test
    @DisplayName("矩阵视图 — 正常返回颜色×尺码矩阵")
    void queryLedgerMatrix_shouldReturnMatrix() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(buildMatrixRow("黑色", "S", 10));
        rows.add(buildMatrixRow("黑色", "M", 20));
        rows.add(buildMatrixRow("黑色", "L", 15));
        rows.add(buildMatrixRow("白色", "S", 5));
        rows.add(buildMatrixRow("白色", "M", 8));
        rows.add(buildMatrixRow("白色", "L", 12));

        when(reportMapper.selectSkuMatrix(1L, 1L)).thenReturn(rows);

        InventoryLedgerMatrixVO result = service.queryLedgerMatrix(1L, 1L);

        assertNotNull(result);
        assertEquals(3, result.getSizes().size());
        assertEquals(List.of("S", "M", "L"), result.getSizes());
        assertEquals(45, result.getColorTotals().get("黑色"));
        assertEquals(25, result.getColorTotals().get("白色"));
        assertEquals(15, result.getSizeTotals().get("S"));
        assertEquals(28, result.getSizeTotals().get("M"));
        assertEquals(27, result.getSizeTotals().get("L"));
        assertEquals(70, result.getGrandTotal());
    }

    @Test
    @DisplayName("矩阵视图 — 无数据返回 null")
    void queryLedgerMatrix_empty_shouldReturnNull() {
        when(reportMapper.selectSkuMatrix(1L, 1L)).thenReturn(Collections.emptyList());

        InventoryLedgerMatrixVO result = service.queryLedgerMatrix(1L, 1L);

        assertNull(result);
    }

    // ==================== 出入库流水 ====================

    @Test
    @DisplayName("出入库流水分页查询")
    void queryOperationFlow_shouldReturnPage() {
        OperationFlowQueryDTO dto = new OperationFlowQueryDTO();
        dto.setCurrent(1L);
        dto.setSize(20L);
        dto.setOperationType("INBOUND_PURCHASE");

        Page<Map<String, Object>> page = new Page<>(1, 20);
        Map<String, Object> row = buildFlowRow();
        page.setRecords(List.of(row));
        page.setTotal(1);

        when(reportMapper.selectOperationFlowPage(any(), isNull(), eq("INBOUND_PURCHASE"),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(page);

        IPage<OperationFlowVO> result = service.queryOperationFlow(dto);

        assertEquals(1, result.getTotal());
        OperationFlowVO vo = result.getRecords().get(0);
        assertEquals("OP20260501001", vo.getOperationNo());
        assertEquals("INBOUND_PURCHASE", vo.getOperationType());
        assertEquals("采购到货", vo.getOperationTypeLabel());
    }

    @Test
    @DisplayName("出入库流水 — 变动数量计算：入库为正")
    void queryOperationFlow_inbound_changeQtyPositive() {
        OperationFlowQueryDTO dto = new OperationFlowQueryDTO();
        dto.setCurrent(1L);
        dto.setSize(20L);

        Page<Map<String, Object>> page = new Page<>(1, 20);
        Map<String, Object> row = buildFlowRow();
        row.put("operationType", "INBOUND_PURCHASE");
        row.put("quantity", new BigDecimal("100"));
        page.setRecords(List.of(row));
        page.setTotal(1);

        when(reportMapper.selectOperationFlowPage(any(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(page);

        IPage<OperationFlowVO> result = service.queryOperationFlow(dto);

        OperationFlowVO vo = result.getRecords().get(0);
        assertEquals(0, vo.getChangeQty().compareTo(new BigDecimal("100")));
    }

    @Test
    @DisplayName("出入库流水 — 变动数量计算：出库为负")
    void queryOperationFlow_outbound_changeQtyNegative() {
        OperationFlowQueryDTO dto = new OperationFlowQueryDTO();
        dto.setCurrent(1L);
        dto.setSize(20L);

        Page<Map<String, Object>> page = new Page<>(1, 20);
        Map<String, Object> row = buildFlowRow();
        row.put("operationType", "OUTBOUND_SALES");
        row.put("quantity", new BigDecimal("50"));
        row.put("changeQty", new BigDecimal("-50"));
        page.setRecords(List.of(row));
        page.setTotal(1);

        when(reportMapper.selectOperationFlowPage(any(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(page);

        IPage<OperationFlowVO> result = service.queryOperationFlow(dto);

        OperationFlowVO vo = result.getRecords().get(0);
        assertEquals(0, vo.getChangeQty().compareTo(new BigDecimal("-50")));
    }

    // ==================== 辅助方法 ====================

    private Map<String, Object> buildSkuLedgerRow() {
        Map<String, Object> row = new HashMap<>();
        row.put("inventoryId", 1L);
        row.put("inventoryType", "SKU");
        row.put("skuId", 1L);
        row.put("skuCode", "SP20260001-BK-M");
        row.put("materialId", null);
        row.put("materialCode", null);
        row.put("materialName", null);
        row.put("spuId", 1L);
        row.put("spuCode", "SP20260001");
        row.put("spuName", "春季连衣裙");
        row.put("colorWayId", 1L);
        row.put("colorName", "黑色");
        row.put("sizeId", 1L);
        row.put("sizeCode", "M");
        row.put("warehouseId", 1L);
        row.put("warehouseName", "主仓库");
        row.put("batchNo", "B001");
        row.put("availableQty", new BigDecimal("80"));
        row.put("lockedQty", new BigDecimal("10"));
        row.put("qcQty", new BigDecimal("5"));
        row.put("totalQty", new BigDecimal("95"));
        row.put("unitCost", new BigDecimal("50.00"));
        row.put("totalAmount", new BigDecimal("4750.00"));
        row.put("lastInboundDate", null);
        row.put("lastOutboundDate", null);
        row.put("updatedAt", LocalDateTime.now());
        return row;
    }

    private Map<String, Object> buildMaterialLedgerRow() {
        Map<String, Object> row = new HashMap<>();
        row.put("inventoryId", 2L);
        row.put("inventoryType", "MATERIAL");
        row.put("skuId", null);
        row.put("skuCode", null);
        row.put("materialId", 1L);
        row.put("materialCode", "MAT001");
        row.put("materialName", "黑色棉布");
        row.put("spuId", null);
        row.put("spuCode", null);
        row.put("spuName", null);
        row.put("colorWayId", null);
        row.put("colorName", null);
        row.put("sizeId", null);
        row.put("sizeCode", null);
        row.put("warehouseId", 1L);
        row.put("warehouseName", "主仓库");
        row.put("batchNo", "B002");
        row.put("availableQty", new BigDecimal("200"));
        row.put("lockedQty", new BigDecimal("0"));
        row.put("qcQty", new BigDecimal("0"));
        row.put("totalQty", new BigDecimal("200"));
        row.put("unitCost", new BigDecimal("15.50"));
        row.put("totalAmount", new BigDecimal("3100.00"));
        row.put("lastInboundDate", null);
        row.put("lastOutboundDate", null);
        row.put("updatedAt", LocalDateTime.now());
        return row;
    }

    private Map<String, Object> buildMatrixRow(String colorName, String sizeCode, int totalQty) {
        Map<String, Object> row = new HashMap<>();
        row.put("colorName", colorName);
        row.put("sizeCode", sizeCode);
        row.put("sortOrder", 0);
        row.put("totalQty", totalQty);
        return row;
    }

    private Map<String, Object> buildFlowRow() {
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1L);
        row.put("operationNo", "OP20260501001");
        row.put("operationType", "INBOUND_PURCHASE");
        row.put("inventoryType", "SKU");
        row.put("skuCode", "SP20260001-BK-M");
        row.put("materialCode", null);
        row.put("materialName", null);
        row.put("warehouseName", "主仓库");
        row.put("batchNo", "B001");
        row.put("quantity", new BigDecimal("100"));
        row.put("totalBefore", new BigDecimal("0"));
        row.put("totalAfter", new BigDecimal("100"));
        row.put("changeQty", new BigDecimal("100"));
        row.put("unitCost", new BigDecimal("50.00"));
        row.put("costAmount", new BigDecimal("5000.00"));
        row.put("sourceType", "ASN");
        row.put("sourceNo", "ASN001");
        row.put("operatorId", 1L);
        row.put("operatedAt", LocalDateTime.now());
        row.put("remark", "到货入库");
        return row;
    }

    // ==================== 库龄分析测试 ====================

    @Test
    @DisplayName("库龄分析 — 成品库龄区间统计")
    void queryAgeAnalysis_sku_shouldClassifyAgeRanges() {
        InventoryAgeQueryDTO dto = new InventoryAgeQueryDTO();
        dto.setInventoryType("SKU");
        dto.setCurrent(1L);
        dto.setSize(20L);

        Page<Map<String, Object>> page = new Page<>(1, 20);
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(buildAgeRow(15));   // 0-30天
        rows.add(buildAgeRow(45));   // 31-60天
        rows.add(buildAgeRow(100));  // 91-180天（超期）
        page.setRecords(rows);
        page.setTotal(3);

        when(reportMapper.selectSkuAgePage(any(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(page);

        // 模拟全量汇总查询（queryAgeAnalysis 内部调用 selectSkuAgeSummary）
        Map<String, Object> ageSummary = new HashMap<>();
        ageSummary.put("totalcount", 3L);
        ageSummary.put("totalqty", new BigDecimal("150"));
        ageSummary.put("totalamount", new BigDecimal("7500.00"));
        ageSummary.put("range0to30count", 1L);
        ageSummary.put("range0to30qty", new BigDecimal("50"));
        ageSummary.put("range31to60count", 1L);
        ageSummary.put("range31to60qty", new BigDecimal("50"));
        ageSummary.put("range61to90count", 0L);
        ageSummary.put("range61to90qty", BigDecimal.ZERO);
        ageSummary.put("range91to180count", 1L);
        ageSummary.put("range91to180qty", new BigDecimal("50"));
        ageSummary.put("range180pluscount", 0L);
        ageSummary.put("range180plusqty", BigDecimal.ZERO);
        ageSummary.put("overduecount", 1L);
        ageSummary.put("overdueqty", new BigDecimal("50"));
        when(reportMapper.selectSkuAgeSummary(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(ageSummary);

        InventoryAgeSummaryVO result = service.queryAgeAnalysis(dto);

        assertEquals(3L, result.getTotalCount());
        assertEquals(1L, result.getAgeRangeCount().get("0-30天"));
        assertEquals(1L, result.getAgeRangeCount().get("31-60天"));
        assertEquals(1L, result.getAgeRangeCount().get("91-180天"));
        assertEquals(1L, result.getOverdueCount()); // 100天 > 90天阈值
    }

    @Test
    @DisplayName("库龄分析 — 超期预警标记")
    void queryAgeAnalysis_overdue_shouldMarkOverdue() {
        InventoryAgeQueryDTO dto = new InventoryAgeQueryDTO();
        dto.setInventoryType("SKU");
        dto.setCurrent(1L);
        dto.setSize(20L);

        Page<Map<String, Object>> page = new Page<>(1, 20);
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(buildAgeRow(200));  // 200天 > 90天 → 超期
        page.setRecords(rows);
        page.setTotal(1);

        when(reportMapper.selectSkuAgePage(any(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(page);

        // 模拟全量汇总查询
        Map<String, Object> ageSummary = new HashMap<>();
        ageSummary.put("totalcount", 1L);
        ageSummary.put("totalqty", new BigDecimal("50"));
        ageSummary.put("totalamount", new BigDecimal("2500.00"));
        ageSummary.put("range0to30count", 0L);
        ageSummary.put("range0to30qty", BigDecimal.ZERO);
        ageSummary.put("range31to60count", 0L);
        ageSummary.put("range31to60qty", BigDecimal.ZERO);
        ageSummary.put("range61to90count", 0L);
        ageSummary.put("range61to90qty", BigDecimal.ZERO);
        ageSummary.put("range91to180count", 0L);
        ageSummary.put("range91to180qty", BigDecimal.ZERO);
        ageSummary.put("range180pluscount", 1L);
        ageSummary.put("range180plusqty", new BigDecimal("50"));
        ageSummary.put("overduecount", 1L);
        ageSummary.put("overdueqty", new BigDecimal("50"));
        when(reportMapper.selectSkuAgeSummary(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(ageSummary);

        InventoryAgeSummaryVO result = service.queryAgeAnalysis(dto);

        InventoryAgeVO detail = result.getDetails().get(0);
        assertEquals(200, detail.getAgeDays());
        assertEquals("180天以上", detail.getAgeRange());
        assertTrue(detail.getOverdue());
    }

    @Test
    @DisplayName("库龄分析 — 无超期")
    void queryAgeAnalysis_noOverdue_shouldNotMark() {
        InventoryAgeQueryDTO dto = new InventoryAgeQueryDTO();
        dto.setInventoryType("SKU");
        dto.setCurrent(1L);
        dto.setSize(20L);

        Page<Map<String, Object>> page = new Page<>(1, 20);
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(buildAgeRow(10));  // 10天 < 90天 → 不超期
        page.setRecords(rows);
        page.setTotal(1);

        when(reportMapper.selectSkuAgePage(any(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(page);

        // 模拟全量汇总查询
        Map<String, Object> ageSummary = new HashMap<>();
        ageSummary.put("totalcount", 1L);
        ageSummary.put("totalqty", new BigDecimal("50"));
        ageSummary.put("totalamount", new BigDecimal("2500.00"));
        ageSummary.put("range0to30count", 1L);
        ageSummary.put("range0to30qty", new BigDecimal("50"));
        ageSummary.put("range31to60count", 0L);
        ageSummary.put("range31to60qty", BigDecimal.ZERO);
        ageSummary.put("range61to90count", 0L);
        ageSummary.put("range61to90qty", BigDecimal.ZERO);
        ageSummary.put("range91to180count", 0L);
        ageSummary.put("range91to180qty", BigDecimal.ZERO);
        ageSummary.put("range180pluscount", 0L);
        ageSummary.put("range180plusqty", BigDecimal.ZERO);
        ageSummary.put("overduecount", 0L);
        ageSummary.put("overdueqty", BigDecimal.ZERO);
        when(reportMapper.selectSkuAgeSummary(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(ageSummary);

        InventoryAgeSummaryVO result = service.queryAgeAnalysis(dto);

        InventoryAgeVO detail = result.getDetails().get(0);
        assertEquals(10, detail.getAgeDays());
        assertEquals("0-30天", detail.getAgeRange());
        assertFalse(detail.getOverdue());
    }

    // ==================== 畅滞销分析测试 ====================

    @Test
    @DisplayName("畅滞销分析 — 畅销品（周转天数<=15）")
    void queryTurnoverAnalysis_fastTurnover_shouldClassifyAsFast() {
        TurnoverQueryDTO dto = new TurnoverQueryDTO();
        dto.setCurrent(1L);
        dto.setSize(20L);
        dto.setStartDate(LocalDate.now().minusDays(30));
        dto.setEndDate(LocalDate.now());

        Page<Map<String, Object>> page = new Page<>(1, 20);
        // avgInventory=(20+20)/2=20, turnoverDays=30*20/500=1.2 → FAST
        Map<String, Object> row = buildTurnoverRow(new BigDecimal("20"), new BigDecimal("500"), new BigDecimal("500"));
        page.setRecords(List.of(row));
        page.setTotal(1);

        when(reportMapper.selectSkuTurnoverPage(any(), isNull(), isNull(), isNull(),
                any(), any(), isNull())).thenReturn(page);

        IPage<TurnoverAnalysisVO> result = service.queryTurnoverAnalysis(dto);

        TurnoverAnalysisVO vo = result.getRecords().get(0);
        assertEquals("FAST", vo.getTurnoverGrade());
        assertEquals("畅销", vo.getTurnoverGradeLabel());
    }

    @Test
    @DisplayName("畅滞销分析 — 呆滞品（无出库）")
    void queryTurnoverAnalysis_noOutbound_shouldClassifyAsDead() {
        TurnoverQueryDTO dto = new TurnoverQueryDTO();
        dto.setCurrent(1L);
        dto.setSize(20L);
        dto.setStartDate(LocalDate.now().minusDays(30));
        dto.setEndDate(LocalDate.now());

        Page<Map<String, Object>> page = new Page<>(1, 20);
        Map<String, Object> row = buildTurnoverRow(new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO);
        page.setRecords(List.of(row));
        page.setTotal(1);

        when(reportMapper.selectSkuTurnoverPage(any(), isNull(), isNull(), isNull(),
                any(), any(), isNull())).thenReturn(page);

        IPage<TurnoverAnalysisVO> result = service.queryTurnoverAnalysis(dto);

        TurnoverAnalysisVO vo = result.getRecords().get(0);
        assertEquals("DEAD", vo.getTurnoverGrade());
        assertEquals("呆滞", vo.getTurnoverGradeLabel());
        assertEquals(0, vo.getTurnoverRate().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("畅滞销分析 — 周转率计算")
    void queryTurnoverAnalysis_shouldCalculateTurnoverRate() {
        TurnoverQueryDTO dto = new TurnoverQueryDTO();
        dto.setCurrent(1L);
        dto.setSize(20L);
        dto.setStartDate(LocalDate.now().minusDays(30));
        dto.setEndDate(LocalDate.now());

        Page<Map<String, Object>> page = new Page<>(1, 20);
        // currentQty=200, outboundQty=300, inboundQty=100
        Map<String, Object> row = buildTurnoverRow(new BigDecimal("200"), new BigDecimal("300"), new BigDecimal("100"));
        page.setRecords(List.of(row));
        page.setTotal(1);

        when(reportMapper.selectSkuTurnoverPage(any(), isNull(), isNull(), isNull(),
                any(), any(), isNull())).thenReturn(page);

        IPage<TurnoverAnalysisVO> result = service.queryTurnoverAnalysis(dto);

        TurnoverAnalysisVO vo = result.getRecords().get(0);
        assertNotNull(vo.getTurnoverRate());
        assertTrue(vo.getTurnoverRate().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(vo.getTurnoverDays());
        assertEquals(Integer.valueOf(30), vo.getPeriodDays());
    }

    // ==================== 辅助方法 ====================

    private Map<String, Object> buildAgeRow(int ageDays) {
        Map<String, Object> row = new HashMap<>();
        row.put("inventoryId", 1L);
        row.put("inventoryType", "SKU");
        row.put("skuCode", "SP20260001-BK-M");
        row.put("materialCode", null);
        row.put("materialName", null);
        row.put("spuCode", "SP20260001");
        row.put("spuName", "春季连衣裙");
        row.put("colorName", "黑色");
        row.put("sizeCode", "M");
        row.put("warehouseName", "主仓库");
        row.put("batchNo", "B001");
        row.put("totalQty", new BigDecimal("50"));
        row.put("totalAmount", new BigDecimal("2500.00"));
        row.put("lastInboundDate", LocalDate.now().minusDays(ageDays));
        row.put("ageDays", ageDays);
        return row;
    }

    private Map<String, Object> buildTurnoverRow(BigDecimal currentQty, BigDecimal outboundQty, BigDecimal inboundQty) {
        Map<String, Object> row = new HashMap<>();
        row.put("skuCode", "SP20260001-BK-M");
        row.put("materialCode", null);
        row.put("materialName", null);
        row.put("spuCode", "SP20260001");
        row.put("spuName", "春季连衣裙");
        row.put("colorName", "黑色");
        row.put("sizeCode", "M");
        row.put("warehouseName", "主仓库");
        row.put("currentQty", currentQty);
        row.put("outboundQty", outboundQty);
        row.put("inboundQty", inboundQty);
        row.put("netOutboundQty", outboundQty.subtract(inboundQty));
        return row;
    }
}
