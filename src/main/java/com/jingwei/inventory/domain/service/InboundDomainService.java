package com.jingwei.inventory.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.repository.InboundOrderLineRepository;
import com.jingwei.inventory.domain.repository.InboundOrderRepository;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.master.domain.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 入库单领域服务
 * <p>
 * 负责入库单的创建和确认入库。
 * 入库确认时调用 {@link InventoryDomainService#changeInventory(ChangeInventoryCommand)} 驱动库存变更。
 * </p>
 * <p>
 * 入库类型与库存操作的映射：
 * <ul>
 *   <li>PURCHASE → INBOUND_PURCHASE（→质检库存）</li>
 *   <li>PRODUCTION → INBOUND_PRODUCTION（→可用库存）</li>
 *   <li>RETURN_SALES → INBOUND_RETURN（→质检库存）</li>
 *   <li>TRANSFER → INBOUND_PRODUCTION（→可用库存）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboundDomainService {

    private final InboundOrderRepository inboundOrderRepository;
    private final InboundOrderLineRepository inboundOrderLineRepository;
    private final InventorySkuRepository inventorySkuRepository;
    private final InventoryMaterialRepository inventoryMaterialRepository;
    private final InventoryDomainService inventoryDomainService;
    private final LocationRepository locationRepository;

    /**
     * 创建入库单
     */
    public InboundOrder createOrder(InboundOrder order, List<InboundOrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BizException(ErrorCode.ORDER_LINE_EMPTY, "入库单至少需要一行明细");
        }

        order.setStatus(InboundStatus.DRAFT);
        inboundOrderRepository.insert(order);

        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).setInboundId(order.getId());
            lines.get(i).setLineNo(i + 1);
        }
        inboundOrderLineRepository.batchInsert(lines);

        order.setLines(lines);
        log.info("创建入库单: inboundNo={}, type={}, warehouseId={}",
                order.getInboundNo(), order.getInboundType(), order.getWarehouseId());
        return order;
    }

    /**
     * 确认入库
     * <p>
     * 遍历入库单行，逐行调用 changeInventory 驱动库存变更。
     * 成品入库时自动创建或更新 inventory_sku 记录。
     * </p>
     */
    public void confirmInbound(Long inboundId, Long operatorId) {
        InboundOrder order = inboundOrderRepository.selectDetailById(inboundId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "入库单不存在");
        }
        if (order.getStatus() != InboundStatus.DRAFT) {
            throw new BizException(ErrorCode.INBOUND_STATUS_INVALID, "只有草稿状态的入库单允许确认");
        }

        for (InboundOrderLine line : order.getLines()) {
            BigDecimal qty = line.getActualQty();
            if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            OperationType opType = order.getInboundType().toOperationType();
            Long inventoryId;

            if (line.getInventoryType() == InventoryType.SKU) {
                inventoryId = findOrCreateSkuInventory(line, order.getWarehouseId());
            } else {
                inventoryId = findOrCreateMaterialInventory(line, order.getWarehouseId());
            }

            ChangeInventoryCommand cmd = new ChangeInventoryCommand();
            cmd.setOperationType(opType);
            cmd.setInventoryType(line.getInventoryType());
            cmd.setInventoryId(inventoryId);
            cmd.setSkuId(line.getSkuId());
            cmd.setMaterialId(line.getMaterialId());
            cmd.setWarehouseId(order.getWarehouseId());
            cmd.setLocationId(line.getLocationId());
            cmd.setBatchNo(line.getBatchNo());
            cmd.setQuantity(qty);
            cmd.setSourceType(order.getSourceType());
            cmd.setSourceId(order.getSourceId());
            cmd.setUnitCost(line.getUnitCost());
            cmd.setOperatorId(operatorId);

            inventoryDomainService.changeInventory(cmd);
        }

        order.setStatus(InboundStatus.CONFIRMED);
        order.setInboundDate(LocalDate.now());
        order.setOperatorId(operatorId);
        inboundOrderRepository.updateById(order);

        log.info("入库确认完成: inboundId={}, inboundNo={}, type={}",
                inboundId, order.getInboundNo(), order.getInboundType());
    }

    /**
     * 查找或创建成品库存记录
     */
    private Long findOrCreateSkuInventory(InboundOrderLine line, Long warehouseId) {
        InventorySku existing = inventorySkuRepository.selectBySkuAndWarehouseAndBatch(
                line.getSkuId(), warehouseId, line.getBatchNo());
        if (existing != null) {
            return existing.getId();
        }
        InventorySku sku = new InventorySku();
        sku.setSkuId(line.getSkuId());
        sku.setWarehouseId(warehouseId);
        sku.setBatchNo(line.getBatchNo() != null ? line.getBatchNo() : "");
        sku.setAvailableQty(0);
        sku.setLockedQty(0);
        sku.setQcQty(0);
        sku.setTotalQty(0);
        sku.setInTransitQty(0);
        sku.setUnitCost(line.getUnitCost() != null ? line.getUnitCost() : BigDecimal.ZERO);
        inventorySkuRepository.insert(sku);
        return sku.getId();
    }

    /**
     * 查找或创建原料库存记录
     */
    private Long findOrCreateMaterialInventory(InboundOrderLine line, Long warehouseId) {
        InventoryMaterial existing = inventoryMaterialRepository.selectByMaterialAndWarehouseAndBatch(
                line.getMaterialId(), warehouseId, line.getBatchNo());
        if (existing != null) {
            return existing.getId();
        }
        InventoryMaterial mat = new InventoryMaterial();
        mat.setMaterialId(line.getMaterialId());
        mat.setWarehouseId(warehouseId);
        mat.setBatchNo(line.getBatchNo() != null ? line.getBatchNo() : "");
        mat.setAvailableQty(BigDecimal.ZERO);
        mat.setLockedQty(BigDecimal.ZERO);
        mat.setQcQty(BigDecimal.ZERO);
        mat.setTotalQty(BigDecimal.ZERO);
        mat.setInTransitQty(BigDecimal.ZERO);
        mat.setUnitCost(line.getUnitCost() != null ? line.getUnitCost() : BigDecimal.ZERO);
        inventoryMaterialRepository.insert(mat);
        return mat.getId();
    }
}
