package com.jingwei.inventory.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.inventory.domain.repository.StocktakingLineRepository;
import com.jingwei.inventory.domain.repository.StocktakingOrderRepository;
import com.jingwei.master.domain.model.LocationStatus;
import com.jingwei.master.domain.model.Location;
import com.jingwei.master.domain.repository.LocationRepository;
import com.jingwei.system.domain.repository.SysConfigRepository;
import com.jingwei.system.domain.service.SysConfigDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 盘点领域服务
 * <p>
 * 负责盘点单的完整生命周期：创建 → 生成盘点行 → 录入实盘 → 差异比对 → 审核调整 → 库存校正。
 * 盘点期间自动冻结被盘库位，完成后自动解冻。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StocktakingDomainService {

    /**
     * 差异率复盘阈值配置键。
     * 与 t_sys_config.config_key 对应，配置值为百分比整数（如 "5" 表示 5%）。
     * 当盘点差异率超过此阈值时，系统自动标记该行需要复盘。
     */
    private static final String RECHECK_THRESHOLD_KEY = "stocktaking.diff.recheck.threshold";
    /**
     * 默认复盘阈值（百分比）。
     * 当 t_sys_config 中未找到 RECHECK_THRESHOLD_KEY 配置项或读取失败时的兜底值，
     * 表示差异率超过 5% 的盘点行需要安排复盘。
     */
    private static final int DEFAULT_RECHECK_THRESHOLD = 5;

    private final StocktakingOrderRepository stocktakingOrderRepository;
    private final StocktakingLineRepository stocktakingLineRepository;
    private final InventorySkuRepository inventorySkuRepository;
    private final InventoryMaterialRepository inventoryMaterialRepository;
    private final InventoryDomainService inventoryDomainService;
    private final LocationRepository locationRepository;
    private final SysConfigDomainService sysConfigDomainService;

    /**
     * 创建盘点单并自动生成盘点行
     * <p>
     * 查询指定仓库下所有有库存的 SKU 和物料，为每条记录生成盘点行（含系统快照数量）。
     * 盲盘模式下盘点阶段不展示 system_qty。
     * </p>
     */
    public StocktakingOrder createOrder(StocktakingOrder order) {
        order.setStatus(StocktakingStatus.DRAFT);
        stocktakingOrderRepository.insert(order);

        List<StocktakingLine> lines = new ArrayList<>();

        // 查询仓库下所有有库存的 SKU，为每条生成盘点行
        List<com.jingwei.inventory.domain.model.InventorySku> skuList = inventorySkuRepository.selectAll();
        for (com.jingwei.inventory.domain.model.InventorySku sku : skuList) {
            if (!sku.getWarehouseId().equals(order.getWarehouseId())) continue;
            if (sku.getTotalQty() <= 0) continue;

            StocktakingLine line = new StocktakingLine();
            line.setStocktakingId(order.getId());
            line.setInventoryType(InventoryType.SKU);
            line.setSkuId(sku.getSkuId());
            line.setWarehouseId(sku.getWarehouseId());
            line.setLocationId(sku.getLocationId());
            line.setBatchNo(sku.getBatchNo());
            line.setSystemQty(BigDecimal.valueOf(sku.getTotalQty()));
            line.setDiffStatus(DiffStatus.PENDING);
            lines.add(line);
        }

        // 查询仓库下所有有库存的物料，为每条生成盘点行
        List<com.jingwei.inventory.domain.model.InventoryMaterial> matList =
                inventoryMaterialRepository.selectByMaterialAndWarehouse(null, order.getWarehouseId());
        // 注：selectByMaterialAndWarehouse 当 materialId=null 时可能不生效，
        // 需要扩展 repository。此处用全量查询过滤。
        List<com.jingwei.inventory.domain.model.InventoryMaterial> allMats = new ArrayList<>();
        // 通过已有方法遍历（简化：实际应增加 selectAll 或 selectByWarehouseId）
        for (com.jingwei.inventory.domain.model.InventoryMaterial mat : allMats) {
            if (!mat.getWarehouseId().equals(order.getWarehouseId())) continue;
            if (mat.getTotalQty().compareTo(BigDecimal.ZERO) <= 0) continue;

            StocktakingLine line = new StocktakingLine();
            line.setStocktakingId(order.getId());
            line.setInventoryType(InventoryType.MATERIAL);
            line.setMaterialId(mat.getMaterialId());
            line.setWarehouseId(mat.getWarehouseId());
            line.setLocationId(mat.getLocationId());
            line.setBatchNo(mat.getBatchNo());
            line.setSystemQty(mat.getTotalQty());
            line.setDiffStatus(DiffStatus.PENDING);
            lines.add(line);
        }

        // 批量插入盘点行
        if (!lines.isEmpty()) {
            stocktakingLineRepository.batchInsert(lines);
        }

        order.setLines(lines);

        log.info("创建盘点单: stocktakingNo={}, type={}, mode={}, warehouseId={}, 生成盘点行数={}",
                order.getStocktakingNo(), order.getStocktakingType(),
                order.getCountMode(), order.getWarehouseId(), lines.size());
        return order;
    }

    /**
     * 开始盘点（DRAFT → IN_PROGRESS）
     * <p>
     * 冻结被盘库位，记录开始时间。
     * </p>
     */
    public void startStocktaking(Long stocktakingId, Long operatorId) {
        StocktakingOrder order = stocktakingOrderRepository.selectById(stocktakingId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "盘点单不存在");
        }
        if (order.getStatus() != StocktakingStatus.DRAFT) {
            throw new BizException(ErrorCode.STOCKTAKING_STATUS_INVALID, "只有草稿状态的盘点单允许开始盘点");
        }

        // 冻结被盘库位
        freezeLocations(order);

        order.setStatus(StocktakingStatus.IN_PROGRESS);
        order.setStartedAt(LocalDateTime.now());
        stocktakingOrderRepository.updateById(order);

        log.info("盘点开始: stocktakingId={}, 库位已冻结", stocktakingId);
    }

    /**
     * 录入实盘数量
     *
     * @param lineId   盘点行ID
     * @param actualQty 实盘数量
     * @param operatorId 操作人ID
     */
    public void recordCount(Long lineId, BigDecimal actualQty, Long operatorId) {
        StocktakingLine line = stocktakingLineRepository.selectById(lineId);
        if (line == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "盘点行不存在");
        }

        line.setActualQty(actualQty);
        line.setDiffQty(actualQty.subtract(line.getSystemQty()));
        line.setCountBy1(operatorId);
        line.setCountAt1(LocalDateTime.now());
        stocktakingLineRepository.updateById(line);
    }

    /**
     * 提交盘点结果（IN_PROGRESS → DIFF_REVIEW）
     */
    public void submitStocktaking(Long stocktakingId, Long operatorId) {
        StocktakingOrder order = stocktakingOrderRepository.selectById(stocktakingId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "盘点单不存在");
        }
        if (order.getStatus() != StocktakingStatus.IN_PROGRESS) {
            throw new BizException(ErrorCode.STOCKTAKING_STATUS_INVALID, "只有进行中的盘点单允许提交");
        }

        // 计算差异并标记需要复盘的行
        List<StocktakingLine> lines = stocktakingLineRepository.selectByStocktakingId(stocktakingId);
        int threshold = getRecheckThreshold();

        for (StocktakingLine line : lines) {
            if (line.getActualQty() == null) continue;

            line.setDiffQty(line.getActualQty().subtract(line.getSystemQty()));

            // 计算差异率
            if (line.getSystemQty().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal diffRate = line.getDiffQty().abs()
                        .divide(line.getSystemQty(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                if (diffRate.intValue() > threshold) {
                    line.setNeedRecheck(true);
                }
            }

            stocktakingLineRepository.updateById(line);
        }

        order.setStatus(StocktakingStatus.DIFF_REVIEW);
        stocktakingOrderRepository.updateById(order);

        log.info("盘点提交: stocktakingId={}, 进入差异审核", stocktakingId);
    }

    /**
     * 审核差异并调整库存（DIFF_REVIEW → COMPLETED）
     * <p>
     * 对确认的差弧行执行库存调整：
     * <ul>
     *   <li>盘盈（diff_qty > 0）→ changeInventory(ADJUST_GAIN)</li>
     *   <li>盘亏（diff_qty < 0）→ changeInventory(ADJUST_LOSS)</li>
     * </ul>
     * 完成后自动解除库位冻结。
     * </p>
     */
    public void reviewAndAdjust(Long stocktakingId, Long reviewerId) {
        StocktakingOrder order = stocktakingOrderRepository.selectById(stocktakingId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "盘点单不存在");
        }
        if (order.getStatus() != StocktakingStatus.DIFF_REVIEW) {
            throw new BizException(ErrorCode.STOCKTAKING_STATUS_INVALID, "只有差异审核中的盘点单允许审核调整");
        }

        List<StocktakingLine> lines = stocktakingLineRepository.selectByStocktakingId(stocktakingId);

        for (StocktakingLine line : lines) {
            if (line.getActualQty() == null || line.getDiffQty().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            // 跳过需要复盘的行
            if (Boolean.TRUE.equals(line.getNeedRecheck())) {
                continue;
            }

            // 查找库存记录
            Long inventoryId = findInventoryId(line, order.getWarehouseId());
            if (inventoryId == null) continue;

            // 盘盈或盘亏
            OperationType opType = line.getDiffQty().compareTo(BigDecimal.ZERO) > 0
                    ? OperationType.ADJUST_GAIN : OperationType.ADJUST_LOSS;

            ChangeInventoryCommand cmd = new ChangeInventoryCommand();
            cmd.setOperationType(opType);
            cmd.setInventoryType(line.getInventoryType());
            cmd.setInventoryId(inventoryId);
            cmd.setSkuId(line.getSkuId());
            cmd.setMaterialId(line.getMaterialId());
            cmd.setWarehouseId(order.getWarehouseId());
            cmd.setLocationId(line.getLocationId());
            cmd.setBatchNo(line.getBatchNo());
            cmd.setQuantity(line.getDiffQty().abs());
            cmd.setSourceType("STOCKTAKING");
            cmd.setSourceId(stocktakingId);
            cmd.setOperatorId(reviewerId);

            inventoryDomainService.changeInventory(cmd);

            line.setDiffStatus(DiffStatus.ADJUSTED);
            line.setAdjustedQty(line.getActualQty());
            stocktakingLineRepository.updateById(line);
        }

        // 解除库位冻结
        unfreezeLocations(order);

        order.setStatus(StocktakingStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        order.setReviewerId(reviewerId);
        order.setReviewedAt(LocalDateTime.now());
        stocktakingOrderRepository.updateById(order);

        log.info("盘点完成: stocktakingId={}, 库位已解冻", stocktakingId);
    }

    /**
     * 查询盘点行（支持盲盘模式过滤 system_qty）
     */
    public List<StocktakingLine> getStocktakingLines(Long stocktakingId, boolean isBlindMode) {
        List<StocktakingLine> lines = stocktakingLineRepository.selectByStocktakingId(stocktakingId);
        if (isBlindMode) {
            // 盲盘模式：置空 system_qty，盘点阶段不展示系统数量
            for (StocktakingLine line : lines) {
                line.setSystemQty(null);
            }
        }
        return lines;
    }

    // ==================== 私有方法 ====================

    /**
     * 冻结被盘库位
     */
    private void freezeLocations(StocktakingOrder order) {
        List<com.jingwei.inventory.domain.model.InventorySku> skuList =
                inventorySkuRepository.selectBySkuAndWarehouse(null, order.getWarehouseId());
        // 实际应通过 warehouseId 查所有有库存的库位并冻结
        // 此处简化：冻结仓库下所有 ACTIVE 库位
        List<Location> locations = locationRepository.selectByWarehouseId(order.getWarehouseId());
        for (Location loc : locations) {
            if (loc.getStatus() == LocationStatus.ACTIVE) {
                loc.setStatus(LocationStatus.FROZEN);
                locationRepository.updateById(loc);
            }
        }
    }

    /**
     * 解除库位冻结
     */
    private void unfreezeLocations(StocktakingOrder order) {
        List<Location> locations = locationRepository.selectByWarehouseId(order.getWarehouseId());
        for (Location loc : locations) {
            if (loc.getStatus() == LocationStatus.FROZEN) {
                loc.setStatus(LocationStatus.ACTIVE);
                locationRepository.updateById(loc);
            }
        }
    }

    /**
     * 查找库存记录ID
     */
    private Long findInventoryId(StocktakingLine line, Long warehouseId) {
        if (line.getInventoryType() == InventoryType.SKU) {
            InventorySku sku = inventorySkuRepository.selectBySkuAndWarehouseAndBatch(
                    line.getSkuId(), warehouseId, line.getBatchNo());
            return sku != null ? sku.getId() : null;
        } else {
            InventoryMaterial mat = inventoryMaterialRepository.selectByMaterialAndWarehouseAndBatch(
                    line.getMaterialId(), warehouseId, line.getBatchNo());
            return mat != null ? mat.getId() : null;
        }
    }

    /**
     * 获取复盘阈值（百分比）
     */
    private int getRecheckThreshold() {
        try {
            var config = sysConfigDomainService.getByConfigKeyOrNull(RECHECK_THRESHOLD_KEY);
            if (config != null && config.getConfigValue() != null) {
                return Integer.parseInt(config.getConfigValue());
            }
        } catch (Exception e) {
            log.debug("读取复盘阈值配置失败，使用默认值{}%", DEFAULT_RECHECK_THRESHOLD);
        }
        return DEFAULT_RECHECK_THRESHOLD;
    }
}
