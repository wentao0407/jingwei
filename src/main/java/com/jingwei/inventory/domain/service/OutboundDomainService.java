package com.jingwei.inventory.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.inventory.domain.repository.OutboundOrderLineRepository;
import com.jingwei.inventory.domain.repository.OutboundOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 出库单领域服务
 * <p>
 * 负责出库单的创建和出库确认。
 * 关键规则：只有 SHIPPED 状态时才扣减库存（货物实际离开仓库）。
 * </p>
 * <p>
 * 出库类型与库存操作的映射：
 * <ul>
 *   <li>SALES → OUTBOUND_SALES（从锁定库存扣减）</li>
 *   <li>MATERIAL → OUTBOUND_MATERIAL（从可用库存扣减）</li>
 *   <li>TRANSFER → OUTBOUND_MATERIAL（从可用库存扣减）</li>
 *   <li>RETURN_PURCHASE → QC_FAIL（从质检库存扣减）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundDomainService {

    private final OutboundOrderRepository outboundOrderRepository;
    private final OutboundOrderLineRepository outboundOrderLineRepository;
    private final InventorySkuRepository inventorySkuRepository;
    private final InventoryMaterialRepository inventoryMaterialRepository;
    private final InventoryDomainService inventoryDomainService;

    /**
     * 创建出库单
     */
    public OutboundOrder createOrder(OutboundOrder order, List<OutboundOrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BizException(ErrorCode.ORDER_LINE_EMPTY, "出库单至少需要一行明细");
        }

        order.setStatus(OutboundStatus.DRAFT);
        outboundOrderRepository.insert(order);

        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).setOutboundId(order.getId());
            lines.get(i).setLineNo(i + 1);
        }
        outboundOrderLineRepository.batchInsert(lines);

        order.setLines(lines);
        log.info("创建出库单: outboundNo={}, type={}, warehouseId={}",
                order.getOutboundNo(), order.getOutboundType(), order.getWarehouseId());
        return order;
    }

    /**
     * 确认出库（发货确认）
     * <p>
     * 只有 SHIPPED 状态才扣减库存。此方法将出库单状态推进到 SHIPPED 并执行库存扣减。
     * </p>
     */
    public void confirmShipped(Long outboundId, Long operatorId) {
        OutboundOrder order = outboundOrderRepository.selectDetailById(outboundId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "出库单不存在");
        }
        if (order.getStatus() != OutboundStatus.DRAFT && order.getStatus() != OutboundStatus.CONFIRMED
                && order.getStatus() != OutboundStatus.PICKING) {
            throw new BizException(ErrorCode.OUTBOUND_STATUS_INVALID,
                    "只有草稿/已确认/拣货中状态的出库单允许发货确认");
        }

        for (OutboundOrderLine line : order.getLines()) {
            BigDecimal qty = line.getActualQty() != null ? line.getActualQty() : line.getPlannedQty();
            if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            OperationType opType = order.getOutboundType().toOperationType();
            Long inventoryId;

            if (line.getInventoryType() == InventoryType.SKU) {
                InventorySku sku = inventorySkuRepository.selectBySkuAndWarehouseAndBatch(
                        line.getSkuId(), order.getWarehouseId(), line.getBatchNo());
                if (sku == null) {
                    throw new BizException(ErrorCode.INVENTORY_NOT_FOUND,
                            "SKU库存记录不存在: skuId=" + line.getSkuId());
                }
                inventoryId = sku.getId();
            } else {
                InventoryMaterial mat = inventoryMaterialRepository.selectByMaterialAndWarehouseAndBatch(
                        line.getMaterialId(), order.getWarehouseId(), line.getBatchNo());
                if (mat == null) {
                    throw new BizException(ErrorCode.INVENTORY_NOT_FOUND,
                            "原料库存记录不存在: materialId=" + line.getMaterialId());
                }
                inventoryId = mat.getId();
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
            cmd.setOperatorId(operatorId);

            inventoryDomainService.changeInventory(cmd);
        }

        order.setStatus(OutboundStatus.SHIPPED);
        order.setOutboundDate(LocalDate.now());
        order.setOperatorId(operatorId);
        outboundOrderRepository.updateById(order);

        log.info("出库确认完成: outboundId={}, outboundNo={}, type={}",
                outboundId, order.getOutboundNo(), order.getOutboundType());
    }
}
