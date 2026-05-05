package com.jingwei.inventory.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.repository.InventoryAllocationRepository;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import com.jingwei.system.domain.service.SysConfigDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 库存预留领域服务
 * <p>
 * 负责销售订单确认时的库存预留（available → locked），
 * 以及订单取消时的释放（locked → available）。
 * </p>
 * <p>
 * 预留策略：
 * <ul>
 *   <li>全额预留：available >= 需求量 → 全部锁定</li>
 *   <li>部分预留：available < 需求量 → 锁定可用部分，返回缺口</li>
 *   <li>无库存：available = 0 → 不预留，返回全部缺口</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationDomainService {

    /** 系统配置键：预留过期天数，对应 t_sys_config.config_key */
    private static final String ALLOCATION_EXPIRY_CONFIG_KEY = "inventory.allocation.expiry.days";
    /** 预留过期默认天数（配置项读取失败时的兜底值） */
    private static final int DEFAULT_EXPIRY_DAYS = 7;

    private final InventorySkuRepository inventorySkuRepository;
    private final InventoryAllocationRepository inventoryAllocationRepository;
    private final InventoryDomainService inventoryDomainService;
    private final SysConfigDomainService sysConfigDomainService;

    /**
     * 库存预留
     * <p>
     * 销售订单确认时调用，为订单行预留可用库存。
     * </p>
     *
     * @param skuId       SKU ID
     * @param warehouseId 仓库ID
     * @param quantity    需求量
     * @param orderType   订单类型（SALES/PRODUCTION）
     * @param orderId     订单ID
     * @param orderLineId 订单行ID
     * @param operatorId  操作人ID
     * @return 预留结果
     */
    public AllocationResult allocate(Long skuId, Long warehouseId, int quantity,
                                      String orderType, Long orderId, Long orderLineId,
                                      Long operatorId) {
        // 查询该 SKU 在指定仓库的库存（默认批次，合并所有批次）
        List<InventorySku> inventories = inventorySkuRepository.selectBySkuAndWarehouse(skuId, warehouseId);
        int totalAvailable = inventories.stream()
                .mapToInt(InventorySku::getAvailableQty)
                .sum();

        if (totalAvailable <= 0) {
            log.info("库存不足，无法预留: skuId={}, warehouseId={}, 需求量={}", skuId, warehouseId, quantity);
            return new AllocationResult(0, quantity, null);
        }

        // 计算实际预留量
        int allocatedQty = Math.min(totalAvailable, quantity);
        int shortfall = quantity - allocatedQty;

        // 从各批次扣减可用库存（按 ID 排序，简单顺序分配）
        int remaining = allocatedQty;
        Long allocationInventoryId = null;
        for (InventorySku inv : inventories) {
            if (remaining <= 0) break;
            int deduct = Math.min(inv.getAvailableQty(), remaining);
            if (deduct > 0) {
                ChangeInventoryCommand cmd = ChangeInventoryCommand.forSku(
                        OperationType.ALLOCATE, inv.getId(),
                        skuId, warehouseId, inv.getBatchNo(), deduct,
                        orderType, orderId, operatorId);
                inventoryDomainService.changeInventory(cmd);
                remaining -= deduct;
                allocationInventoryId = inv.getId();
            }
        }

        // 创建预留记录
        int expiryDays = getExpiryDays();
        InventoryAllocation allocation = new InventoryAllocation();
        allocation.setOrderType(orderType);
        allocation.setOrderId(orderId);
        allocation.setOrderLineId(orderLineId);
        allocation.setSkuId(skuId);
        allocation.setWarehouseId(warehouseId);
        allocation.setAllocatedQty(BigDecimal.valueOf(allocatedQty));
        allocation.setFulfilledQty(BigDecimal.ZERO);
        allocation.setRemainingQty(BigDecimal.valueOf(allocatedQty));
        allocation.setStatus(AllocationStatus.ACTIVE);
        allocation.setExpireAt(LocalDateTime.now().plusDays(expiryDays));
        allocation.setCreatedBy(operatorId);
        allocation.setCreatedAt(LocalDateTime.now());
        inventoryAllocationRepository.insert(allocation);

        log.info("库存预留完成: skuId={}, allocated={}, shortfall={}, allocationId={}",
                skuId, allocatedQty, shortfall, allocation.getId());

        return new AllocationResult(allocatedQty, shortfall, allocation.getId());
    }

    /**
     * 释放预留（订单取消时调用）
     * <p>
     * 将预留的库存归还到可用库存（locked → available）。
     * </p>
     *
     * @param allocationId 预留记录ID
     * @param operatorId   操作人ID
     */
    public void release(Long allocationId, Long operatorId) {
        InventoryAllocation allocation = inventoryAllocationRepository.selectById(allocationId);
        if (allocation == null) {
            throw new BizException(ErrorCode.ALLOCATION_NOT_FOUND);
        }

        if (allocation.getStatus() != AllocationStatus.ACTIVE
                && allocation.getStatus() != AllocationStatus.PARTIAL_FULFILLED) {
            throw new BizException(ErrorCode.ALLOCATION_STATUS_INVALID,
                    "只有预留中或部分出库的记录允许释放");
        }

        BigDecimal releaseQty = allocation.getRemainingQty();
        if (releaseQty.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // 释放库存（locked → available）
        ChangeInventoryCommand cmd = ChangeInventoryCommand.forSku(
                OperationType.RELEASE, null,
                allocation.getSkuId(), allocation.getWarehouseId(),
                allocation.getBatchNo(), releaseQty.intValue(),
                allocation.getOrderType(), allocation.getOrderId(), operatorId);

        // 查找库存记录ID
        List<InventorySku> inventories = inventorySkuRepository.selectBySkuAndWarehouse(
                allocation.getSkuId(), allocation.getWarehouseId());
        for (InventorySku inv : inventories) {
            if (inv.getLockedQty() > 0) {
                cmd.setInventoryId(inv.getId());
                inventoryDomainService.changeInventory(cmd);
                break;
            }
        }

        // 更新预留状态
        allocation.setStatus(AllocationStatus.RELEASED);
        allocation.setRemainingQty(BigDecimal.ZERO);
        allocation.setUpdatedBy(operatorId);
        allocation.setUpdatedAt(LocalDateTime.now());
        inventoryAllocationRepository.updateById(allocation);

        log.info("库存预留释放: allocationId={}, releaseQty={}", allocationId, releaseQty);
    }

    /**
     * 出库完成（出库时调用，更新预留记录的已出库量）
     *
     * @param allocationId 预留记录ID
     * @param outboundQty  本次出库量
     * @param operatorId   操作人ID
     */
    public void fulfill(Long allocationId, int outboundQty, Long operatorId) {
        InventoryAllocation allocation = inventoryAllocationRepository.selectById(allocationId);
        if (allocation == null) {
            throw new BizException(ErrorCode.ALLOCATION_NOT_FOUND);
        }

        allocation.setFulfilledQty(allocation.getFulfilledQty().add(BigDecimal.valueOf(outboundQty)));
        allocation.setRemainingQty(allocation.getAllocatedQty().subtract(allocation.getFulfilledQty()));

        if (allocation.getRemainingQty().compareTo(BigDecimal.ZERO) <= 0) {
            allocation.setStatus(AllocationStatus.FULFILLED);
            allocation.setRemainingQty(BigDecimal.ZERO);
        } else {
            allocation.setStatus(AllocationStatus.PARTIAL_FULFILLED);
        }

        allocation.setUpdatedBy(operatorId);
        allocation.setUpdatedAt(LocalDateTime.now());
        inventoryAllocationRepository.updateById(allocation);

        log.info("预留出库完成: allocationId={}, outboundQty={}, newStatus={}",
                allocationId, outboundQty, allocation.getStatus());
    }

    /**
     * 过期自动释放（定时任务调用）
     * <p>
     * 扫描所有已过期的 ACTIVE 预留记录，自动释放库存。
     * </p>
     *
     * @param operatorId 操作人ID（系统操作使用固定ID）
     * @return 释放的预留记录数
     */
    public int releaseExpired(Long operatorId) {
        List<InventoryAllocation> expiredList = inventoryAllocationRepository.selectExpiredActive();
        int count = 0;
        for (InventoryAllocation allocation : expiredList) {
            try {
                release(allocation.getId(), operatorId);
                // release 方法已经将状态设置为 RELEASED，这里再设置为 EXPIRED
                // 用于区分正常释放和过期释放
                InventoryAllocation updated = inventoryAllocationRepository.selectById(allocation.getId());
                if (updated != null) {
                    updated.setStatus(AllocationStatus.EXPIRED);
                    inventoryAllocationRepository.updateById(updated);
                }
                count++;
            } catch (Exception e) {
                log.warn("过期预留释放失败: allocationId={}, error={}", allocation.getId(), e.getMessage());
            }
        }
        if (count > 0) {
            log.info("过期预留自动释放: 共释放{}条记录", count);
        }
        return count;
    }

    /**
     * 查询订单的预留记录
     */
    public List<InventoryAllocation> getOrderAllocations(String orderType, Long orderId) {
        return inventoryAllocationRepository.selectByOrder(orderType, orderId);
    }

    // ==================== 私有方法 ====================

    private int getExpiryDays() {
        try {
            var config = sysConfigDomainService.getByConfigKeyOrNull(ALLOCATION_EXPIRY_CONFIG_KEY);
            if (config != null && config.getConfigValue() != null) {
                return Integer.parseInt(config.getConfigValue());
            }
        } catch (Exception e) {
            log.debug("读取预留过期天数配置失败，使用默认值{}天", DEFAULT_EXPIRY_DAYS);
        }
        return DEFAULT_EXPIRY_DAYS;
    }

    /**
     * 预留结果
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class AllocationResult {
        /** 实际预留量 */
        private final int allocatedQty;
        /** 缺口数量 */
        private final int shortfall;
        /** 预留记录ID（无库存时为null） */
        private final Long allocationId;
    }
}
