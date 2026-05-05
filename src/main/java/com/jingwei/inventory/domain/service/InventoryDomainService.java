package com.jingwei.inventory.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.inventory.domain.repository.InventoryOperationRepository;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 库存领域服务 — 核心库存变更服务
 * <p>
 * 所有库存变更必须通过 {@link #changeInventory(ChangeInventoryCommand)} 方法，
 * 禁止直接 UPDATE inventory_sku / inventory_material 的数量字段。
 * </p>
 * <p>
 * 核心流程：
 * <ol>
 *   <li>加载库存记录</li>
 *   <li>记录 before 快照</li>
 *   <li>校验操作合法性</li>
 *   <li>按操作类型更新字段</li>
 *   <li>重算 total = available + locked + qc</li>
 *   <li>乐观锁保存（最多重试3次，退避 50~150ms）</li>
 *   <li>写操作流水（含 before/after 快照）</li>
 * </ol>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryDomainService {

    /** 乐观锁最大重试次数 */
    private static final int MAX_RETRIES = 3;
    /** 重试退避最小等待时间（毫秒） */
    private static final int MIN_BACKOFF_MS = 50;
    /** 重试退避最大等待时间（毫秒） */
    private static final int MAX_BACKOFF_MS = 150;

    private final InventorySkuRepository inventorySkuRepository;
    private final InventoryMaterialRepository inventoryMaterialRepository;
    private final InventoryOperationRepository inventoryOperationRepository;

    /**
     * 通用库存变更方法 — 所有库存操作的唯一入口
     *
     * @param cmd 变更命令
     */
    public void changeInventory(ChangeInventoryCommand cmd) {
        if (cmd.getInventoryType() == InventoryType.SKU) {
            changeSkuInventory(cmd);
        } else {
            changeMaterialInventory(cmd);
        }
    }

    /**
     * 成品库存变更
     */
    private void changeSkuInventory(ChangeInventoryCommand cmd) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                // 1. 加载库存记录
                InventorySku record = inventorySkuRepository.selectById(cmd.getInventoryId());
                if (record == null) {
                    throw new BizException(ErrorCode.INVENTORY_NOT_FOUND);
                }

                // 2. 记录 before 快照
                int availableBefore = record.getAvailableQty();
                int lockedBefore = record.getLockedQty();
                int qcBefore = record.getQcQty();
                int totalBefore = record.getTotalQty();

                // 3. 校验操作合法性
                validateSkuChange(cmd.getOperationType(), cmd.getQuantity().intValue(), record);

                // 4. 执行变更
                applySkuChange(record, cmd.getOperationType(), cmd.getQuantity().intValue());

                // 5. 重算 total
                record.setTotalQty(record.getAvailableQty() + record.getLockedQty() + record.getQcQty());

                // 6. 更新最后出入库日期
                updateDateFields(record, cmd.getOperationType());

                // 7. 乐观锁保存
                int rows = inventorySkuRepository.updateById(record);
                if (rows == 0) {
                    throw new BizException(ErrorCode.CONCURRENT_CONFLICT, "库存记录已被其他操作修改");
                }

                // 8. 写操作流水
                InventoryOperation operation = buildSkuOperation(cmd, record,
                        availableBefore, lockedBefore, qcBefore, totalBefore);
                inventoryOperationRepository.insert(operation);

                log.info("成品库存变更: skuId={}, warehouseId={}, opType={}, qty={}, {}→{}",
                        cmd.getSkuId(), cmd.getWarehouseId(), cmd.getOperationType(),
                        cmd.getQuantity(), availableBefore, record.getAvailableQty());
                return;

            } catch (BizException e) {
                if (e.getCode() == ErrorCode.CONCURRENT_CONFLICT.getCode()) {
                    if (attempt == MAX_RETRIES - 1) {
                        log.warn("成品库存乐观锁冲突，{}次重试仍失败: inventoryId={}", MAX_RETRIES, cmd.getInventoryId());
                        throw new BizException(ErrorCode.CONCURRENT_CONFLICT,
                                "库存操作并发冲突，请稍后重试");
                    }
                    backoff(attempt);
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * 原料库存变更
     */
    private void changeMaterialInventory(ChangeInventoryCommand cmd) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                InventoryMaterial record = inventoryMaterialRepository.selectById(cmd.getInventoryId());
                if (record == null) {
                    throw new BizException(ErrorCode.INVENTORY_NOT_FOUND);
                }

                BigDecimal availableBefore = record.getAvailableQty();
                BigDecimal lockedBefore = record.getLockedQty();
                BigDecimal qcBefore = record.getQcQty();
                BigDecimal totalBefore = record.getTotalQty();

                validateMaterialChange(cmd.getOperationType(), cmd.getQuantity(), record);
                applyMaterialChange(record, cmd.getOperationType(), cmd.getQuantity());

                record.setTotalQty(record.getAvailableQty().add(record.getLockedQty()).add(record.getQcQty()));
                updateMaterialDateFields(record, cmd.getOperationType());

                int rows = inventoryMaterialRepository.updateById(record);
                if (rows == 0) {
                    throw new BizException(ErrorCode.CONCURRENT_CONFLICT, "库存记录已被其他操作修改");
                }

                InventoryOperation operation = buildMaterialOperation(cmd, record,
                        availableBefore, lockedBefore, qcBefore, totalBefore);
                inventoryOperationRepository.insert(operation);

                log.info("原料库存变更: materialId={}, warehouseId={}, opType={}, qty={}, {}→{}",
                        cmd.getMaterialId(), cmd.getWarehouseId(), cmd.getOperationType(),
                        cmd.getQuantity(), availableBefore, record.getAvailableQty());
                return;

            } catch (BizException e) {
                if (e.getCode() == ErrorCode.CONCURRENT_CONFLICT.getCode()) {
                    if (attempt == MAX_RETRIES - 1) {
                        throw new BizException(ErrorCode.CONCURRENT_CONFLICT, "库存操作并发冲突，请稍后重试");
                    }
                    backoff(attempt);
                } else {
                    throw e;
                }
            }
        }
    }

    // ==================== 校验逻辑 ====================

    /**
     * 校验成品库存变更合法性
     */
    private void validateSkuChange(OperationType opType, int qty, InventorySku record) {
        switch (opType) {
            case ALLOCATE:
            case OUTBOUND_MATERIAL:
            case ADJUST_LOSS:
                if (record.getAvailableQty() < qty) {
                    throw new BizException(ErrorCode.INSUFFICIENT_INVENTORY,
                            "可用库存不足，当前" + record.getAvailableQty() + "，需要" + qty);
                }
                break;
            case OUTBOUND_SALES:
                if (record.getLockedQty() < qty) {
                    throw new BizException(ErrorCode.LOCKED_INVENTORY_INSUFFICIENT,
                            "锁定库存不足，当前" + record.getLockedQty() + "，需要" + qty);
                }
                break;
            case QC_PASS:
            case QC_FAIL:
                if (record.getQcQty() < qty) {
                    throw new BizException(ErrorCode.QC_INVENTORY_INSUFFICIENT,
                            "质检库存不足，当前" + record.getQcQty() + "，需要" + qty);
                }
                break;
            case RELEASE:
                if (record.getLockedQty() < qty) {
                    throw new BizException(ErrorCode.LOCKED_INVENTORY_INSUFFICIENT,
                            "锁定库存不足，无法释放" + qty);
                }
                break;
            // INBOUND_* 不需要校验上限
        }
    }

    /**
     * 校验原料库存变更合法性
     */
    private void validateMaterialChange(OperationType opType, BigDecimal qty, InventoryMaterial record) {
        switch (opType) {
            case ALLOCATE:
            case OUTBOUND_MATERIAL:
            case ADJUST_LOSS:
                if (record.getAvailableQty().compareTo(qty) < 0) {
                    throw new BizException(ErrorCode.INSUFFICIENT_INVENTORY,
                            "可用库存不足，当前" + record.getAvailableQty() + "，需要" + qty);
                }
                break;
            case OUTBOUND_SALES:
                if (record.getLockedQty().compareTo(qty) < 0) {
                    throw new BizException(ErrorCode.LOCKED_INVENTORY_INSUFFICIENT,
                            "锁定库存不足");
                }
                break;
            case QC_PASS:
            case QC_FAIL:
                if (record.getQcQty().compareTo(qty) < 0) {
                    throw new BizException(ErrorCode.QC_INVENTORY_INSUFFICIENT,
                            "质检库存不足");
                }
                break;
            case RELEASE:
                if (record.getLockedQty().compareTo(qty) < 0) {
                    throw new BizException(ErrorCode.LOCKED_INVENTORY_INSUFFICIENT,
                            "锁定库存不足，无法释放");
                }
                break;
        }
    }

    // ==================== 字段变更逻辑 ====================

    /**
     * 按操作类型更新成品库存字段
     */
    private void applySkuChange(InventorySku record, OperationType opType, int qty) {
        switch (opType) {
            case INBOUND_PURCHASE:
            case INBOUND_RETURN:
                record.setQcQty(record.getQcQty() + qty);
                break;
            case INBOUND_PRODUCTION:
                record.setAvailableQty(record.getAvailableQty() + qty);
                break;
            case QC_PASS:
                record.setQcQty(record.getQcQty() - qty);
                record.setAvailableQty(record.getAvailableQty() + qty);
                break;
            case QC_FAIL:
                record.setQcQty(record.getQcQty() - qty);
                break;
            case ALLOCATE:
                record.setAvailableQty(record.getAvailableQty() - qty);
                record.setLockedQty(record.getLockedQty() + qty);
                break;
            case RELEASE:
                record.setAvailableQty(record.getAvailableQty() + qty);
                record.setLockedQty(record.getLockedQty() - qty);
                break;
            case OUTBOUND_SALES:
                record.setLockedQty(record.getLockedQty() - qty);
                break;
            case OUTBOUND_MATERIAL:
                record.setAvailableQty(record.getAvailableQty() - qty);
                break;
            case ADJUST_GAIN:
                record.setAvailableQty(record.getAvailableQty() + qty);
                break;
            case ADJUST_LOSS:
                record.setAvailableQty(record.getAvailableQty() - qty);
                break;
        }
    }

    /**
     * 按操作类型更新原料库存字段
     */
    private void applyMaterialChange(InventoryMaterial record, OperationType opType, BigDecimal qty) {
        switch (opType) {
            case INBOUND_PURCHASE:
            case INBOUND_RETURN:
                record.setQcQty(record.getQcQty().add(qty));
                break;
            case INBOUND_PRODUCTION:
                record.setAvailableQty(record.getAvailableQty().add(qty));
                break;
            case QC_PASS:
                record.setQcQty(record.getQcQty().subtract(qty));
                record.setAvailableQty(record.getAvailableQty().add(qty));
                break;
            case QC_FAIL:
                record.setQcQty(record.getQcQty().subtract(qty));
                break;
            case ALLOCATE:
                record.setAvailableQty(record.getAvailableQty().subtract(qty));
                record.setLockedQty(record.getLockedQty().add(qty));
                break;
            case RELEASE:
                record.setAvailableQty(record.getAvailableQty().add(qty));
                record.setLockedQty(record.getLockedQty().subtract(qty));
                break;
            case OUTBOUND_SALES:
                record.setLockedQty(record.getLockedQty().subtract(qty));
                break;
            case OUTBOUND_MATERIAL:
                record.setAvailableQty(record.getAvailableQty().subtract(qty));
                break;
            case ADJUST_GAIN:
                record.setAvailableQty(record.getAvailableQty().add(qty));
                break;
            case ADJUST_LOSS:
                record.setAvailableQty(record.getAvailableQty().subtract(qty));
                break;
        }
    }

    // ==================== 日期字段更新 ====================

    private void updateDateFields(InventorySku record, OperationType opType) {
        if (opType == OperationType.INBOUND_PURCHASE || opType == OperationType.INBOUND_PRODUCTION
                || opType == OperationType.INBOUND_RETURN) {
            record.setLastInboundDate(java.time.LocalDate.now());
        } else if (opType == OperationType.OUTBOUND_SALES || opType == OperationType.OUTBOUND_MATERIAL) {
            record.setLastOutboundDate(java.time.LocalDate.now());
        }
    }

    private void updateMaterialDateFields(InventoryMaterial record, OperationType opType) {
        if (opType == OperationType.INBOUND_PURCHASE || opType == OperationType.INBOUND_PRODUCTION
                || opType == OperationType.INBOUND_RETURN) {
            record.setLastInboundDate(java.time.LocalDate.now());
        } else if (opType == OperationType.OUTBOUND_SALES || opType == OperationType.OUTBOUND_MATERIAL) {
            record.setLastOutboundDate(java.time.LocalDate.now());
        }
    }

    // ==================== 操作流水构建 ====================

    private InventoryOperation buildSkuOperation(ChangeInventoryCommand cmd, InventorySku after,
                                                  int availableBefore, int lockedBefore,
                                                  int qcBefore, int totalBefore) {
        InventoryOperation op = new InventoryOperation();
        op.setOperationType(cmd.getOperationType());
        op.setInventoryType(InventoryType.SKU);
        op.setInventoryId(cmd.getInventoryId());
        op.setSkuId(cmd.getSkuId());
        op.setWarehouseId(cmd.getWarehouseId());
        op.setLocationId(cmd.getLocationId());
        op.setBatchNo(cmd.getBatchNo());
        op.setQuantity(cmd.getQuantity());
        op.setAvailableBefore(BigDecimal.valueOf(availableBefore));
        op.setAvailableAfter(BigDecimal.valueOf(after.getAvailableQty()));
        op.setLockedBefore(BigDecimal.valueOf(lockedBefore));
        op.setLockedAfter(BigDecimal.valueOf(after.getLockedQty()));
        op.setQcBefore(BigDecimal.valueOf(qcBefore));
        op.setQcAfter(BigDecimal.valueOf(after.getQcQty()));
        op.setTotalBefore(BigDecimal.valueOf(totalBefore));
        op.setTotalAfter(BigDecimal.valueOf(after.getTotalQty()));
        op.setSourceType(cmd.getSourceType());
        op.setSourceId(cmd.getSourceId());
        op.setSourceNo(cmd.getSourceNo());
        op.setUnitCost(cmd.getUnitCost() != null ? cmd.getUnitCost() : BigDecimal.ZERO);
        op.setCostAmount(op.getUnitCost().multiply(cmd.getQuantity()));
        op.setOperatorId(cmd.getOperatorId());
        op.setOperatedAt(LocalDateTime.now());
        op.setRemark(cmd.getRemark());
        return op;
    }

    private InventoryOperation buildMaterialOperation(ChangeInventoryCommand cmd, InventoryMaterial after,
                                                       BigDecimal availableBefore, BigDecimal lockedBefore,
                                                       BigDecimal qcBefore, BigDecimal totalBefore) {
        InventoryOperation op = new InventoryOperation();
        op.setOperationType(cmd.getOperationType());
        op.setInventoryType(InventoryType.MATERIAL);
        op.setInventoryId(cmd.getInventoryId());
        op.setMaterialId(cmd.getMaterialId());
        op.setWarehouseId(cmd.getWarehouseId());
        op.setLocationId(cmd.getLocationId());
        op.setBatchNo(cmd.getBatchNo());
        op.setQuantity(cmd.getQuantity());
        op.setAvailableBefore(availableBefore);
        op.setAvailableAfter(after.getAvailableQty());
        op.setLockedBefore(lockedBefore);
        op.setLockedAfter(after.getLockedQty());
        op.setQcBefore(qcBefore);
        op.setQcAfter(after.getQcQty());
        op.setTotalBefore(totalBefore);
        op.setTotalAfter(after.getTotalQty());
        op.setSourceType(cmd.getSourceType());
        op.setSourceId(cmd.getSourceId());
        op.setSourceNo(cmd.getSourceNo());
        op.setUnitCost(cmd.getUnitCost() != null ? cmd.getUnitCost() : BigDecimal.ZERO);
        op.setCostAmount(op.getUnitCost().multiply(cmd.getQuantity()));
        op.setOperatorId(cmd.getOperatorId());
        op.setOperatedAt(LocalDateTime.now());
        op.setRemark(cmd.getRemark());
        return op;
    }

    // ==================== 工具方法 ====================

    /**
     * 退避等待（随机 50~150ms）
     */
    private void backoff(int attempt) {
        try {
            int sleepMs = ThreadLocalRandom.current().nextInt(MIN_BACKOFF_MS, MAX_BACKOFF_MS + 1);
            log.debug("乐观锁冲突，第{}次重试，等待{}ms", attempt + 1, sleepMs);
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
