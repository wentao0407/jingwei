package com.jingwei.report.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.report.application.dto.InventoryAgeQueryDTO;
import com.jingwei.report.application.dto.InventoryLedgerQueryDTO;
import com.jingwei.report.application.dto.OperationFlowQueryDTO;
import com.jingwei.report.application.dto.TurnoverQueryDTO;
import com.jingwei.report.application.service.ReportApplicationService;
import com.jingwei.report.interfaces.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 报表 Controller
 * <p>
 * 提供库存台账、出入库流水的查询与导出接口。
 * </p>
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportApplicationService reportApplicationService;

    // ==================== 库存台账 ====================

    /**
     * 分页查询库存台账
     * <p>
     * inventoryType=SKU 查询成品库存，inventoryType=MATERIAL 查询原料库存。
     * 支持按仓库、品类、季节、SKU/物料ID、关键字筛选。
     * </p>
     */
    @RequirePermission("report:ledger:view")
    @PostMapping("/report/ledger/page")
    public R<IPage<InventoryLedgerVO>> queryLedger(@Valid @RequestBody InventoryLedgerQueryDTO dto) {
        return R.ok(reportApplicationService.queryLedger(dto));
    }

    /**
     * 查询库存台账矩阵视图（颜色 × 尺码）
     * <p>
     * 指定款式和仓库，返回颜色为行、尺码为列的库存数量矩阵。
     * 适用于成品库存的可视化展示。
     * </p>
     */
    @RequirePermission("report:ledger:view")
    @PostMapping("/report/ledger/matrix")
    public R<InventoryLedgerMatrixVO> queryLedgerMatrix(@RequestParam Long spuId,
                                                         @RequestParam Long warehouseId) {
        InventoryLedgerMatrixVO matrix = reportApplicationService.queryLedgerMatrix(spuId, warehouseId);
        if (matrix == null) {
            return R.ok("无库存数据", null);
        }
        return R.ok(matrix);
    }

    /**
     * 导出库存台账到 Excel
     */
    @RequirePermission("report:ledger:export")
    @PostMapping("/report/ledger/export")
    public void exportLedger(@Valid @RequestBody InventoryLedgerQueryDTO dto,
                              HttpServletResponse response) throws IOException {
        reportApplicationService.exportLedger(dto, response);
    }

    // ==================== 出入库流水 ====================

    /**
     * 分页查询出入库流水
     * <p>
     * 支持按库存类型、操作类型、仓库、SKU/物料、来源单据、时间范围、操作单号筛选。
     * </p>
     */
    @RequirePermission("report:flow:view")
    @PostMapping("/report/flow/page")
    public R<IPage<OperationFlowVO>> queryOperationFlow(@Valid @RequestBody OperationFlowQueryDTO dto) {
        return R.ok(reportApplicationService.queryOperationFlow(dto));
    }

    /**
     * 导出出入库流水到 Excel
     */
    @RequirePermission("report:flow:export")
    @PostMapping("/report/flow/export")
    public void exportOperationFlow(@Valid @RequestBody OperationFlowQueryDTO dto,
                                     HttpServletResponse response) throws IOException {
        reportApplicationService.exportOperationFlow(dto, response);
    }

    // ==================== 库龄分析 ====================

    /**
     * 查询库龄分析（汇总 + 明细）
     * <p>
     * 返回各库龄区间的统计汇总和分页明细。
     * 库龄 = 当前日期 - 最后入库日期，超期阈值默认90天。
     * </p>
     */
    @RequirePermission("report:age:view")
    @PostMapping("/report/age/summary")
    public R<InventoryAgeSummaryVO> queryAgeAnalysis(@Valid @RequestBody InventoryAgeQueryDTO dto) {
        return R.ok(reportApplicationService.queryAgeAnalysis(dto));
    }

    /**
     * 导出库龄分析到 Excel
     */
    @RequirePermission("report:age:export")
    @PostMapping("/report/age/export")
    public void exportAgeAnalysis(@Valid @RequestBody InventoryAgeQueryDTO dto,
                                   HttpServletResponse response) throws IOException {
        reportApplicationService.exportAgeAnalysis(dto, response);
    }

    // ==================== 畅滞销分析 ====================

    /**
     * 查询畅滞销分析
     * <p>
     * 统计指定时间范围内的出库数量，计算周转天数和周转率。
     * 畅销等级：FAST(<=15天)、NORMAL(16-60天)、SLOW(61-90天)、DEAD(>90天)。
     * </p>
     */
    @RequirePermission("report:turnover:view")
    @PostMapping("/report/turnover/page")
    public R<IPage<TurnoverAnalysisVO>> queryTurnoverAnalysis(@Valid @RequestBody TurnoverQueryDTO dto) {
        return R.ok(reportApplicationService.queryTurnoverAnalysis(dto));
    }

    /**
     * 导出畅滞销分析到 Excel
     */
    @RequirePermission("report:turnover:export")
    @PostMapping("/report/turnover/export")
    public void exportTurnoverAnalysis(@Valid @RequestBody TurnoverQueryDTO dto,
                                        HttpServletResponse response) throws IOException {
        reportApplicationService.exportTurnoverAnalysis(dto, response);
    }
}
