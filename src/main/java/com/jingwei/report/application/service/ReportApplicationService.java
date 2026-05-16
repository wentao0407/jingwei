package com.jingwei.report.application.service;

import cn.hutool.core.map.MapUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.inventory.domain.model.OperationType;
import com.jingwei.report.application.dto.InventoryAgeQueryDTO;
import com.jingwei.report.application.dto.InventoryLedgerQueryDTO;
import com.jingwei.report.application.dto.OperationFlowQueryDTO;
import com.jingwei.report.application.dto.TurnoverQueryDTO;
import com.jingwei.report.infrastructure.persistence.ReportMapper;
import com.jingwei.report.interfaces.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 报表应用服务
 * <p>
 * 负责库存台账、出入库流水的查询与导出。
 * 查询通过 ReportMapper 的原生 SQL 实现跨表关联，
 * 导出通过 Hutool ExcelWriter 生成 Excel 文件。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportApplicationService {

    /**
     * 库存操作类型枚举到中文标签的映射。
     * 基于 OperationType 枚举构建，用于出入库流水报表中将操作编码（如 INBOUND_PURCHASE）转换为中文显示（如"采购入库"）。
     * 静态初始化块构建，运行期间不可变。
     */
    private static final Map<String, String> OP_TYPE_LABELS;

    static {
        OP_TYPE_LABELS = new HashMap<>();
        for (OperationType type : OperationType.values()) {
            OP_TYPE_LABELS.put(type.getCode(), type.getLabel());
        }
    }

    private final ReportMapper reportMapper;

    // ==================== 库存台账 ====================

    /**
     * 查询库存台账（自动区分成品/原料）
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    public IPage<InventoryLedgerVO> queryLedger(InventoryLedgerQueryDTO dto) {
        Page<?> page = new Page<>(dto.getCurrent(), dto.getSize());
        String inventoryType = dto.getInventoryType();

        if ("MATERIAL".equalsIgnoreCase(inventoryType)) {
            return queryMaterialLedger(page, dto);
        } else {
            // 默认查询成品
            return querySkuLedger(page, dto);
        }
    }

    /**
     * 成品库存台账分页查询
     */
    private IPage<InventoryLedgerVO> querySkuLedger(Page<?> page, InventoryLedgerQueryDTO dto) {
        IPage<Map<String, Object>> result = reportMapper.selectSkuLedgerPage(
                page, dto.getWarehouseId(), dto.getSkuId(),
                dto.getCategoryId(), dto.getSeasonId(), dto.getKeyword());

        return result.convert(this::mapToLedgerVO);
    }

    /**
     * 原料库存台账分页查询
     */
    private IPage<InventoryLedgerVO> queryMaterialLedger(Page<?> page, InventoryLedgerQueryDTO dto) {
        IPage<Map<String, Object>> result = reportMapper.selectMaterialLedgerPage(
                page, dto.getWarehouseId(), dto.getMaterialId(), dto.getKeyword());

        return result.convert(this::mapToLedgerVO);
    }

    /**
     * 查询库存台账矩阵视图（颜色 × 尺码）
     *
     * @param spuId      款式ID
     * @param warehouseId 仓库ID
     * @return 矩阵视图
     */
    public InventoryLedgerMatrixVO queryLedgerMatrix(Long spuId, Long warehouseId) {
        List<Map<String, Object>> rows = reportMapper.selectSkuMatrix(spuId, warehouseId);
        if (rows.isEmpty()) {
            return null;
        }

        // 收集所有尺码（按 sortOrder 去重排序）
        List<String> sizes = new ArrayList<>();
        Set<String> sizeSet = new LinkedHashSet<>();
        for (Map<String, Object> row : rows) {
            String sizeCode = MapUtil.getStr(row, "sizeCode");
            if (sizeCode != null) {
                sizeSet.add(sizeCode);
            }
        }
        sizes.addAll(sizeSet);

        // 组装矩阵：colorName → (sizeCode → qty)
        Map<String, Map<String, Integer>> matrix = new LinkedHashMap<>();
        Map<String, Integer> colorTotals = new LinkedHashMap<>();
        Map<String, Integer> sizeTotals = new LinkedHashMap<>();
        int grandTotal = 0;

        // 初始化 sizeTotals
        for (String size : sizes) {
            sizeTotals.put(size, 0);
        }

        for (Map<String, Object> row : rows) {
            String colorName = MapUtil.getStr(row, "colorName");
            String sizeCode = MapUtil.getStr(row, "sizeCode");
            int qty = MapUtil.getInt(row, "totalQty", 0);

            matrix.computeIfAbsent(colorName, k -> new LinkedHashMap<>());
            matrix.get(colorName).put(sizeCode, qty);

            // 颜色行汇总
            colorTotals.merge(colorName, qty, Integer::sum);

            // 尺码列汇总
            sizeTotals.merge(sizeCode, qty, Integer::sum);

            grandTotal += qty;
        }

        InventoryLedgerMatrixVO vo = new InventoryLedgerMatrixVO();
        vo.setSpuId(spuId);
        vo.setWarehouseId(warehouseId);
        vo.setSizes(sizes);
        vo.setMatrix(matrix);
        vo.setColorTotals(colorTotals);
        vo.setSizeTotals(sizeTotals);
        vo.setGrandTotal(grandTotal);
        return vo;
    }

    /**
     * 导出库存台账到 Excel
     *
     * @param dto      查询条件
     * @param response HTTP 响应
     */
    public void exportLedger(InventoryLedgerQueryDTO dto, HttpServletResponse response) throws IOException {
        String inventoryType = dto.getInventoryType();
        List<Map<String, Object>> data;

        if ("MATERIAL".equalsIgnoreCase(inventoryType)) {
            data = reportMapper.selectMaterialLedgerExport(
                    dto.getWarehouseId(), dto.getMaterialId(), dto.getKeyword());
        } else {
            data = reportMapper.selectSkuLedgerExport(
                    dto.getWarehouseId(), dto.getSkuId(),
                    dto.getCategoryId(), dto.getSeasonId(), dto.getKeyword());
        }

        writeExcel(response, "库存台账", buildLedgerHeaders(inventoryType), buildLedgerRows(data, inventoryType));
    }

    // ==================== 出入库流水 ====================

    /**
     * 查询出入库流水分页
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    public IPage<OperationFlowVO> queryOperationFlow(OperationFlowQueryDTO dto) {
        Page<?> page = new Page<>(dto.getCurrent(), dto.getSize());

        IPage<Map<String, Object>> result = reportMapper.selectOperationFlowPage(
                page, dto.getInventoryType(), dto.getOperationType(),
                dto.getWarehouseId(), dto.getSkuId(), dto.getMaterialId(),
                dto.getSourceType(), dto.getStartTime(), dto.getEndTime(),
                dto.getOperationNo());

        return result.convert(this::mapToFlowVO);
    }

    /**
     * 导出出入库流水到 Excel
     *
     * @param dto      查询条件
     * @param response HTTP 响应
     */
    public void exportOperationFlow(OperationFlowQueryDTO dto, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> data = reportMapper.selectOperationFlowExport(
                dto.getInventoryType(), dto.getOperationType(),
                dto.getWarehouseId(), dto.getSkuId(), dto.getMaterialId(),
                dto.getSourceType(), dto.getStartTime(), dto.getEndTime(),
                dto.getOperationNo());

        List<String> headers = List.of("操作单号", "操作类型", "库存类型", "SKU/物料编码", "物料名称",
                "仓库", "批次号", "操作数量", "变动数量", "操作前库存", "操作后库存",
                "单位成本", "成本金额", "来源单据", "来源编号", "操作时间", "备注");
        List<List<Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : data) {
            rows.add(List.of(
                    MapUtil.getStr(row, "operationNo"),
                    OP_TYPE_LABELS.getOrDefault(MapUtil.getStr(row, "operationType"), MapUtil.getStr(row, "operationType")),
                    MapUtil.getStr(row, "inventoryType"),
                    coalesce(MapUtil.getStr(row, "skuCode"), MapUtil.getStr(row, "materialCode")),
                    MapUtil.getStr(row, "materialName"),
                    MapUtil.getStr(row, "warehouseName"),
                    MapUtil.getStr(row, "batchNo"),
                    row.get("quantity"),
                    computeChangeQty(MapUtil.getStr(row, "operationType"), row.get("quantity")),
                    row.get("totalBefore"),
                    row.get("totalAfter"),
                    row.get("unitCost"),
                    row.get("costAmount"),
                    MapUtil.getStr(row, "sourceType"),
                    MapUtil.getStr(row, "sourceNo"),
                    row.get("operatedAt"),
                    MapUtil.getStr(row, "remark")
            ));
        }

        writeExcel(response, "出入库流水", headers, rows);
    }

    // ==================== 私有方法 ====================

    /**
     * 将 Map 行转换为库存台账 VO
     */
    private InventoryLedgerVO mapToLedgerVO(Map<String, Object> row) {
        InventoryLedgerVO vo = new InventoryLedgerVO();
        vo.setInventoryId(toLong(row.get("inventoryId")));
        vo.setInventoryType(MapUtil.getStr(row, "inventoryType"));
        vo.setSkuId(toLong(row.get("skuId")));
        vo.setSkuCode(MapUtil.getStr(row, "skuCode"));
        vo.setMaterialId(toLong(row.get("materialId")));
        vo.setMaterialCode(MapUtil.getStr(row, "materialCode"));
        vo.setMaterialName(MapUtil.getStr(row, "materialName"));
        vo.setSpuId(toLong(row.get("spuId")));
        vo.setSpuCode(MapUtil.getStr(row, "spuCode"));
        vo.setSpuName(MapUtil.getStr(row, "spuName"));
        vo.setColorWayId(toLong(row.get("colorWayId")));
        vo.setColorName(MapUtil.getStr(row, "colorName"));
        vo.setSizeId(toLong(row.get("sizeId")));
        vo.setSizeCode(MapUtil.getStr(row, "sizeCode"));
        vo.setWarehouseId(toLong(row.get("warehouseId")));
        vo.setWarehouseName(MapUtil.getStr(row, "warehouseName"));
        vo.setBatchNo(MapUtil.getStr(row, "batchNo"));
        vo.setAvailableQty(toBigDecimal(row.get("availableQty")));
        vo.setLockedQty(toBigDecimal(row.get("lockedQty")));
        vo.setQcQty(toBigDecimal(row.get("qcQty")));
        vo.setTotalQty(toBigDecimal(row.get("totalQty")));
        vo.setUnitCost(toBigDecimal(row.get("unitCost")));
        vo.setTotalAmount(toBigDecimal(row.get("totalAmount")));
        vo.setLastInboundDate(toLocalDate(row.get("lastInboundDate")));
        vo.setLastOutboundDate(toLocalDate(row.get("lastOutboundDate")));
        vo.setUpdatedAt(toLocalDateTime(row.get("updatedAt")));
        return vo;
    }

    /**
     * 将 Map 行转换为出入库流水 VO
     */
    private OperationFlowVO mapToFlowVO(Map<String, Object> row) {
        OperationFlowVO vo = new OperationFlowVO();
        vo.setId(toLong(row.get("id")));
        vo.setOperationNo(MapUtil.getStr(row, "operationNo"));
        String opType = MapUtil.getStr(row, "operationType");
        vo.setOperationType(opType);
        vo.setOperationTypeLabel(OP_TYPE_LABELS.getOrDefault(opType, opType));
        vo.setInventoryType(MapUtil.getStr(row, "inventoryType"));
        vo.setSkuCode(MapUtil.getStr(row, "skuCode"));
        vo.setMaterialCode(MapUtil.getStr(row, "materialCode"));
        vo.setMaterialName(MapUtil.getStr(row, "materialName"));
        vo.setWarehouseName(MapUtil.getStr(row, "warehouseName"));
        vo.setBatchNo(MapUtil.getStr(row, "batchNo"));
        vo.setQuantity(toBigDecimal(row.get("quantity")));
        vo.setTotalBefore(toBigDecimal(row.get("totalBefore")));
        vo.setTotalAfter(toBigDecimal(row.get("totalAfter")));
        vo.setChangeQty(toBigDecimal(row.get("changeQty")));
        vo.setUnitCost(toBigDecimal(row.get("unitCost")));
        vo.setCostAmount(toBigDecimal(row.get("costAmount")));
        vo.setSourceType(MapUtil.getStr(row, "sourceType"));
        vo.setSourceNo(MapUtil.getStr(row, "sourceNo"));
        vo.setOperatorId(toLong(row.get("operatorId")));
        vo.setOperatedAt(toLocalDateTime(row.get("operatedAt")));
        vo.setRemark(MapUtil.getStr(row, "remark"));
        return vo;
    }

    /**
     * 构建库存台账 Excel 表头
     */
    private List<String> buildLedgerHeaders(String inventoryType) {
        if ("MATERIAL".equalsIgnoreCase(inventoryType)) {
            return List.of("物料编码", "物料名称", "仓库", "批次号",
                    "可用数量", "锁定数量", "质检数量", "实际库存",
                    "单位成本", "库存金额", "最后入库日期", "最后出库日期");
        }
        return List.of("SKU编码", "款式编码", "款式名称", "颜色", "尺码", "仓库", "批次号",
                "可用数量", "锁定数量", "质检数量", "实际库存",
                "单位成本", "库存金额", "最后入库日期", "最后出库日期");
    }

    /**
     * 构建库存台账 Excel 行数据
     */
    private List<List<Object>> buildLedgerRows(List<Map<String, Object>> data, String inventoryType) {
        List<List<Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : data) {
            if ("MATERIAL".equalsIgnoreCase(inventoryType)) {
                rows.add(List.of(
                        MapUtil.getStr(row, "materialCode"),
                        MapUtil.getStr(row, "materialName"),
                        MapUtil.getStr(row, "warehouseName"),
                        MapUtil.getStr(row, "batchNo"),
                        row.get("availableQty"),
                        row.get("lockedQty"),
                        row.get("qcQty"),
                        row.get("totalQty"),
                        row.get("unitCost"),
                        row.get("totalAmount"),
                        row.get("lastInboundDate"),
                        row.get("lastOutboundDate")
                ));
            } else {
                rows.add(List.of(
                        MapUtil.getStr(row, "skuCode"),
                        MapUtil.getStr(row, "spuCode"),
                        MapUtil.getStr(row, "spuName"),
                        MapUtil.getStr(row, "colorName"),
                        MapUtil.getStr(row, "sizeCode"),
                        MapUtil.getStr(row, "warehouseName"),
                        MapUtil.getStr(row, "batchNo"),
                        row.get("availableQty"),
                        row.get("lockedQty"),
                        row.get("qcQty"),
                        row.get("totalQty"),
                        row.get("unitCost"),
                        row.get("totalAmount"),
                        row.get("lastInboundDate"),
                        row.get("lastOutboundDate")
                ));
            }
        }
        return rows;
    }

    /**
     * 写出 Excel 文件到响应流
     */
    private void writeExcel(HttpServletResponse response, String sheetName,
                             List<String> headers, List<List<Object>> rows) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(sheetName + ".xlsx", StandardCharsets.UTF_8));

        try (ExcelWriter writer = ExcelUtil.getWriter(true)) {
            // 写表头
            writer.writeHeadRow(headers);
            // 写数据
            for (List<Object> row : rows) {
                writer.writeRow(row);
            }
            writer.flush(response.getOutputStream(), true);
        }
    }

    /**
     * 计算变动数量（入库为正，出库为负）
     */
    private BigDecimal computeChangeQty(String operationType, Object quantity) {
        BigDecimal qty = toBigDecimal(quantity);
        if (qty == null) return BigDecimal.ZERO;

        // 出库和扣减类操作返回负数
        if ("QC_FAIL".equals(operationType) || "OUTBOUND_SALES".equals(operationType)
                || "OUTBOUND_MATERIAL".equals(operationType) || "ADJUST_LOSS".equals(operationType)) {
            return qty.negate();
        }
        // 分配/释放不改变总量
        if ("ALLOCATE".equals(operationType) || "RELEASE".equals(operationType)) {
            return BigDecimal.ZERO;
        }
        return qty;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Number n) return n.longValue();
        return null;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(value.toString());
    }

    private java.time.LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof java.time.LocalDate ld) return ld;
        return java.time.LocalDate.parse(value.toString());
    }

    private java.time.LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof java.time.LocalDateTime ldt) return ldt;
        return java.time.LocalDateTime.parse(value.toString());
    }

    private String coalesce(String a, String b) {
        return a != null ? a : b;
    }

    // ==================== 库龄分析 ====================

    /**
     * 默认库龄超期阈值（天）。
     * 当 t_sys_config 中未配置库龄超期天数时的兜底值，超过此天数的库存标记为"超期"并触发预警。
     * 服装行业通常以 90 天作为库龄健康分界线。
     */
    private static final int DEFAULT_OVERDUE_DAYS = 90;

    /**
     * 查询库龄分析（汇总 + 明细）
     *
     * @param dto 查询条件
     * @return 库龄分析汇总
     */
    public InventoryAgeSummaryVO queryAgeAnalysis(InventoryAgeQueryDTO dto) {
        Page<?> page = new Page<>(dto.getCurrent(), dto.getSize());
        String inventoryType = dto.getInventoryType();

        IPage<Map<String, Object>> result;
        if ("MATERIAL".equalsIgnoreCase(inventoryType)) {
            result = reportMapper.selectMaterialAgePage(page, dto.getWarehouseId(), dto.getKeyword());
        } else {
            result = reportMapper.selectSkuAgePage(page, dto.getWarehouseId(), null,
                    dto.getCategoryId(), dto.getSeasonId(), dto.getKeyword());
        }

        // 转换明细（分页）
        List<InventoryAgeVO> details = result.getRecords().stream()
                .map(this::mapToAgeVO)
                .toList();

        // 构建汇总（使用全量聚合查询，不依赖分页结果）
        InventoryAgeSummaryVO summary = new InventoryAgeSummaryVO();
        summary.setDetails(details);
        summary.setTotalCount(result.getTotal());

        Map<String, Object> fullSummary;
        if ("MATERIAL".equalsIgnoreCase(inventoryType)) {
            fullSummary = reportMapper.selectMaterialAgeSummary(
                    dto.getWarehouseId(), dto.getKeyword());
        } else {
            fullSummary = reportMapper.selectSkuAgeSummary(
                    dto.getWarehouseId(), null, dto.getCategoryId(), dto.getSeasonId(), dto.getKeyword());
        }

        // 从全量汇总结果中提取数据
        Map<String, Long> ageRangeCount = new LinkedHashMap<>();
        Map<String, BigDecimal> ageRangeQty = new LinkedHashMap<>();
        Map<String, BigDecimal> ageRangeAmount = new LinkedHashMap<>();

        ageRangeCount.put("0-30天", toLongDefault0(fullSummary.get("range0to30count")));
        ageRangeQty.put("0-30天", toBigDecimalDefault0(fullSummary.get("range0to30qty")));
        ageRangeCount.put("31-60天", toLongDefault0(fullSummary.get("range31to60count")));
        ageRangeQty.put("31-60天", toBigDecimalDefault0(fullSummary.get("range31to60qty")));
        ageRangeCount.put("61-90天", toLongDefault0(fullSummary.get("range61to90count")));
        ageRangeQty.put("61-90天", toBigDecimalDefault0(fullSummary.get("range61to90qty")));
        ageRangeCount.put("91-180天", toLongDefault0(fullSummary.get("range91to180count")));
        ageRangeQty.put("91-180天", toBigDecimalDefault0(fullSummary.get("range91to180qty")));
        ageRangeCount.put("180天以上", toLongDefault0(fullSummary.get("range180pluscount")));
        ageRangeQty.put("180天以上", toBigDecimalDefault0(fullSummary.get("range180plusqty")));

        summary.setAgeRangeCount(ageRangeCount);
        summary.setAgeRangeQty(ageRangeQty);
        summary.setAgeRangeAmount(ageRangeAmount);
        summary.setTotalCount(toLongDefault0(fullSummary.get("totalcount")));
        summary.setTotalQty(toBigDecimalDefault0(fullSummary.get("totalqty")));
        summary.setTotalAmount(toBigDecimalDefault0(fullSummary.get("totalamount")));
        summary.setOverdueCount(toLongDefault0(fullSummary.get("overduecount")));
        summary.setOverdueQty(toBigDecimalDefault0(fullSummary.get("overdueqty")));

        return summary;
    }

    private long toLongDefault0(Object val) {
        if (val instanceof Number) return ((Number) val).longValue();
        return 0L;
    }

    private BigDecimal toBigDecimalDefault0(Object val) {
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        return BigDecimal.ZERO;
    }

    /**
     * 导出库龄分析到 Excel
     */
    public void exportAgeAnalysis(InventoryAgeQueryDTO dto, HttpServletResponse response) throws IOException {
        // 导出使用不分页查询（复用分页查询去掉分页参数）
        dto.setCurrent(1L);
        dto.setSize(Long.MAX_VALUE);
        InventoryAgeSummaryVO summary = queryAgeAnalysis(dto);

        List<String> headers = List.of("SKU/物料编码", "款式编码", "款式名称", "颜色", "尺码",
                "仓库", "批次号", "实际库存", "库存金额", "最后入库日期", "库龄天数", "库龄区间", "是否超期");
        List<List<Object>> rows = new ArrayList<>();
        for (InventoryAgeVO vo : summary.getDetails()) {
            rows.add(List.of(
                    coalesce(vo.getSkuCode(), vo.getMaterialCode()),
                    vo.getSpuCode(),
                    vo.getSpuName(),
                    vo.getColorName(),
                    vo.getSizeCode(),
                    vo.getWarehouseName(),
                    vo.getBatchNo(),
                    vo.getTotalQty(),
                    vo.getTotalAmount(),
                    vo.getLastInboundDate(),
                    vo.getAgeDays(),
                    vo.getAgeRange(),
                    Boolean.TRUE.equals(vo.getOverdue()) ? "是" : "否"
            ));
        }

        writeExcel(response, "库龄分析", headers, rows);
    }

    /**
     * 将 Map 行转换为库龄 VO，计算库龄区间和超期标记
     */
    private InventoryAgeVO mapToAgeVO(Map<String, Object> row) {
        InventoryAgeVO vo = new InventoryAgeVO();
        vo.setInventoryId(toLong(row.get("inventoryId")));
        vo.setInventoryType(MapUtil.getStr(row, "inventoryType"));
        vo.setSkuCode(MapUtil.getStr(row, "skuCode"));
        vo.setMaterialCode(MapUtil.getStr(row, "materialCode"));
        vo.setMaterialName(MapUtil.getStr(row, "materialName"));
        vo.setSpuCode(MapUtil.getStr(row, "spuCode"));
        vo.setSpuName(MapUtil.getStr(row, "spuName"));
        vo.setColorName(MapUtil.getStr(row, "colorName"));
        vo.setSizeCode(MapUtil.getStr(row, "sizeCode"));
        vo.setWarehouseName(MapUtil.getStr(row, "warehouseName"));
        vo.setBatchNo(MapUtil.getStr(row, "batchNo"));
        vo.setTotalQty(toBigDecimal(row.get("totalQty")));
        vo.setTotalAmount(toBigDecimal(row.get("totalAmount")));
        vo.setLastInboundDate(toLocalDate(row.get("lastInboundDate")));

        // 计算库龄天数
        Integer ageDays = toInteger(row.get("ageDays"));
        vo.setAgeDays(ageDays);

        // 计算库龄区间
        vo.setAgeRange(classifyAgeRange(ageDays));

        // 超期标记
        vo.setOverdue(ageDays != null && ageDays > DEFAULT_OVERDUE_DAYS);

        return vo;
    }

    /**
     * 库龄天数分类到区间
     */
    private String classifyAgeRange(Integer ageDays) {
        if (ageDays == null) return "0-30天";
        if (ageDays <= 30) return "0-30天";
        if (ageDays <= 60) return "31-60天";
        if (ageDays <= 90) return "61-90天";
        if (ageDays <= 180) return "91-180天";
        return "180天以上";
    }

    // ==================== 畅滞销分析 ====================

    /**
     * 畅销等级：周转天数 ≤ 15 天。
     * 对应畅滞销分析报表中的分级标准，用于标识快速流转的 SKU/物料。
     */
    private static final String GRADE_FAST = "FAST";
    /**
     * 畅销等级：周转天数 16-60 天。
     * 正常流转速度，属于健康库存水平。
     */
    private static final String GRADE_NORMAL = "NORMAL";
    /**
     * 畅销等级：周转天数 61-90 天。
     * 流转偏慢，需关注是否即将进入滞销区间。
     */
    private static final String GRADE_SLOW = "SLOW";
    /**
     * 畅销等级：周转天数 > 90 天或统计周期内无出库记录。
     * 滞销/死库存，建议促销或清仓处理。
     */
    private static final String GRADE_DEAD = "DEAD";

    /**
     * 畅销等级编码到中文标签的映射。
     * 用于畅滞销分析报表的 VO 层展示：FAST→"畅销"、NORMAL→"正常"、SLOW→"滞销"、DEAD→"死货"。
     */
    private static final Map<String, String> GRADE_LABELS = Map.of(
            GRADE_FAST, "畅销",
            GRADE_NORMAL, "正常",
            GRADE_SLOW, "滞销",
            GRADE_DEAD, "呆滞"
    );

    /**
     * 查询畅滞销分析
     *
     * @param dto 查询条件
     * @return 畅滞销分析分页结果
     */
    public IPage<TurnoverAnalysisVO> queryTurnoverAnalysis(TurnoverQueryDTO dto) {
        // 默认统计最近30天
        LocalDate endDate = dto.getEndDate() != null ? dto.getEndDate() : LocalDate.now();
        LocalDate startDate = dto.getStartDate() != null ? dto.getStartDate() : endDate.minusDays(30);
        int rawPeriodDays = (int) ChronoUnit.DAYS.between(startDate, endDate);
        final int periodDays = rawPeriodDays > 0 ? rawPeriodDays : 30;

        Page<?> page = new Page<>(dto.getCurrent(), dto.getSize());
        String inventoryType = dto.getInventoryType();

        IPage<Map<String, Object>> result;
        if ("MATERIAL".equalsIgnoreCase(inventoryType)) {
            result = reportMapper.selectMaterialTurnoverPage(page, dto.getWarehouseId(),
                    startDate, endDate, dto.getKeyword());
        } else {
            result = reportMapper.selectSkuTurnoverPage(page, dto.getWarehouseId(), null,
                    dto.getCategoryId(), dto.getSeasonId(),
                    startDate, endDate, dto.getKeyword());
        }

        IPage<TurnoverAnalysisVO> voPage = result.convert(row -> mapToTurnoverVO(row, startDate, endDate, periodDays));
        return voPage;
    }

    /**
     * 导出畅滞销分析到 Excel
     */
    public void exportTurnoverAnalysis(TurnoverQueryDTO dto, HttpServletResponse response) throws IOException {
        dto.setCurrent(1L);
        dto.setSize(Long.MAX_VALUE);
        IPage<TurnoverAnalysisVO> pageResult = queryTurnoverAnalysis(dto);

        List<String> headers = List.of("SKU/物料编码", "款式编码", "款式名称", "颜色", "尺码",
                "仓库", "当前库存", "出库数量", "入库数量", "净出库",
                "平均库存", "周转天数", "周转率", "畅销等级");
        List<List<Object>> rows = new ArrayList<>();
        for (TurnoverAnalysisVO vo : pageResult.getRecords()) {
            rows.add(List.of(
                    coalesce(vo.getSkuCode(), vo.getMaterialCode()),
                    vo.getSpuCode(),
                    vo.getSpuName(),
                    vo.getColorName(),
                    vo.getSizeCode(),
                    vo.getWarehouseName(),
                    vo.getCurrentQty(),
                    vo.getOutboundQty(),
                    vo.getInboundQty(),
                    vo.getNetOutboundQty(),
                    vo.getAvgInventory(),
                    vo.getTurnoverDays(),
                    vo.getTurnoverRate(),
                    vo.getTurnoverGradeLabel()
            ));
        }

        writeExcel(response, "畅滞销分析", headers, rows);
    }

    /**
     * 将 Map 行转换为畅滞销 VO，计算周转指标
     */
    private TurnoverAnalysisVO mapToTurnoverVO(Map<String, Object> row,
                                                 LocalDate startDate, LocalDate endDate, int periodDays) {
        TurnoverAnalysisVO vo = new TurnoverAnalysisVO();
        vo.setSkuCode(MapUtil.getStr(row, "skuCode"));
        vo.setMaterialCode(MapUtil.getStr(row, "materialCode"));
        vo.setMaterialName(MapUtil.getStr(row, "materialName"));
        vo.setSpuCode(MapUtil.getStr(row, "spuCode"));
        vo.setSpuName(MapUtil.getStr(row, "spuName"));
        vo.setColorName(MapUtil.getStr(row, "colorName"));
        vo.setSizeCode(MapUtil.getStr(row, "sizeCode"));
        vo.setWarehouseName(MapUtil.getStr(row, "warehouseName"));

        BigDecimal currentQty = toBigDecimal(row.get("currentQty"));
        BigDecimal outboundQty = toBigDecimal(row.get("outboundQty"));
        BigDecimal inboundQty = toBigDecimal(row.get("inboundQty"));
        BigDecimal netOutbound = toBigDecimal(row.get("netOutboundQty"));

        vo.setCurrentQty(currentQty);
        vo.setOutboundQty(outboundQty);
        vo.setInboundQty(inboundQty);
        vo.setNetOutboundQty(netOutbound);
        vo.setStartDate(startDate);
        vo.setEndDate(endDate);
        vo.setPeriodDays(periodDays);

        // 平均库存 = (期初 + 期末) / 2
        // 期初库存 ≈ 当前库存 + 净出库（简化估算）
        BigDecimal safeCurrent = currentQty != null ? currentQty : BigDecimal.ZERO;
        BigDecimal safeNetOut = netOutbound != null ? netOutbound : BigDecimal.ZERO;
        BigDecimal beginQty = safeCurrent.add(safeNetOut);
        BigDecimal avgInventory = beginQty.add(safeCurrent)
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        vo.setAvgInventory(avgInventory);

        // 周转天数和周转率
        if (outboundQty != null && outboundQty.compareTo(BigDecimal.ZERO) > 0
                && avgInventory.compareTo(BigDecimal.ZERO) > 0) {
            // 周转天数 = 统计天数 × 平均库存 / 出库数量
            BigDecimal turnoverDays = BigDecimal.valueOf(periodDays)
                    .multiply(avgInventory)
                    .divide(outboundQty, 1, RoundingMode.HALF_UP);
            vo.setTurnoverDays(turnoverDays);

            // 周转率 = 出库数量 / 平均库存
            BigDecimal turnoverRate = outboundQty
                    .divide(avgInventory, 2, RoundingMode.HALF_UP);
            vo.setTurnoverRate(turnoverRate);

            // 畅销等级
            vo.setTurnoverGrade(classifyTurnoverGrade(turnoverDays.intValue()));
        } else {
            // 无出库记录视为呆滞
            vo.setTurnoverDays(BigDecimal.valueOf(999));
            vo.setTurnoverRate(BigDecimal.ZERO);
            vo.setTurnoverGrade(GRADE_DEAD);
        }
        vo.setTurnoverGradeLabel(GRADE_LABELS.get(vo.getTurnoverGrade()));

        return vo;
    }

    /**
     * 根据周转天数分类畅销等级
     */
    private String classifyTurnoverGrade(int turnoverDays) {
        if (turnoverDays <= 15) return GRADE_FAST;
        if (turnoverDays <= 60) return GRADE_NORMAL;
        if (turnoverDays <= 90) return GRADE_SLOW;
        return GRADE_DEAD;
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        return null;
    }
}
