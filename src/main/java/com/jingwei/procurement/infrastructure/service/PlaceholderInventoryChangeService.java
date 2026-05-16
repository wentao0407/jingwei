package com.jingwei.procurement.infrastructure.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.model.InTransitStatus;
import com.jingwei.inventory.domain.model.InventoryInTransit;
import com.jingwei.inventory.domain.model.InventoryMaterial;
import com.jingwei.inventory.domain.model.InventoryType;
import com.jingwei.inventory.domain.model.OperationType;
import com.jingwei.inventory.domain.repository.InventoryInTransitRepository;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.inventory.domain.service.ChangeInventoryCommand;
import com.jingwei.inventory.domain.service.InventoryDomainService;
import com.jingwei.procurement.domain.service.InventoryChangeContext;
import com.jingwei.procurement.domain.service.InventoryChangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存变更服务真实实现
 * <p>
 * 采购模块的库存流转：收货（在途→质检）、检验合格（质检→可用）、检验不合格（质检出库）。
 * 通过 InventoryDomainService 执行实际库存变更。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceholderInventoryChangeService implements InventoryChangeService {

    private final InventoryDomainService inventoryDomainService;
    private final InventoryMaterialRepository inventoryMaterialRepository;
    private final InventoryInTransitRepository inventoryInTransitRepository;

    @Override
    public void inTransitToQc(Long materialId, BigDecimal quantity) {
        inTransitToQc(new InventoryChangeContext(materialId, null, null, null, quantity));
    }

    @Override
    public void inTransitToQc(InventoryChangeContext context) {
        InventoryInTransit inTransit = resolveInTransit(context);
        InventoryMaterial record = resolveOrCreateMaterialRecord(context, inTransit.getWarehouseId());
        ChangeInventoryCommand cmd = buildCommand(OperationType.INBOUND_PURCHASE, context, record);
        inventoryDomainService.changeInventory(cmd);
        updateInTransitReceivedQty(inTransit, context.quantity());
        log.info("在途→质检完成: materialId={}, warehouseId={}, batchNo={}, quantity={}",
                context.materialId(), record.getWarehouseId(), record.getBatchNo(), context.quantity());
    }

    @Override
    public void qcToAvailable(Long materialId, BigDecimal quantity) {
        qcToAvailable(new InventoryChangeContext(materialId, null, null, null, quantity));
    }

    @Override
    public void qcToAvailable(InventoryChangeContext context) {
        InventoryMaterial record = resolveExistingMaterialRecord(context);
        ChangeInventoryCommand cmd = buildCommand(OperationType.QC_PASS, context, record);
        inventoryDomainService.changeInventory(cmd);
        log.info("质检→可用完成: materialId={}, warehouseId={}, batchNo={}, quantity={}",
                context.materialId(), record.getWarehouseId(), record.getBatchNo(), context.quantity());
    }

    @Override
    public void qcOut(Long materialId, BigDecimal quantity) {
        qcOut(new InventoryChangeContext(materialId, null, null, null, quantity));
    }

    @Override
    public void qcOut(InventoryChangeContext context) {
        InventoryMaterial record = resolveExistingMaterialRecord(context);
        ChangeInventoryCommand cmd = buildCommand(OperationType.QC_FAIL, context, record);
        inventoryDomainService.changeInventory(cmd);
        log.info("质检出库（退货）完成: materialId={}, warehouseId={}, batchNo={}, quantity={}",
                context.materialId(), record.getWarehouseId(), record.getBatchNo(), context.quantity());
    }

    private InventoryInTransit resolveInTransit(InventoryChangeContext context) {
        List<InventoryInTransit> records = context.procurementLineId() != null
                ? inventoryInTransitRepository.selectByProcurementLineId(context.procurementLineId())
                : inventoryInTransitRepository.selectByMaterialId(context.materialId());
        return records.stream()
                .filter(record -> context.materialId().equals(record.getMaterialId()))
                .filter(record -> context.warehouseId() == null || context.warehouseId().equals(record.getWarehouseId()))
                .findFirst()
                .orElseThrow(() -> new BizException(ErrorCode.INVENTORY_NOT_FOUND, "未找到匹配的在途库存记录"));
    }

    private InventoryMaterial resolveExistingMaterialRecord(InventoryChangeContext context) {
        Long warehouseId = resolveWarehouseId(context);
        InventoryMaterial record = inventoryMaterialRepository.selectByMaterialAndWarehouseAndBatch(
                context.materialId(), warehouseId, normalizeBatchNo(context.batchNo()));
        if (record == null) {
            throw new BizException(ErrorCode.INVENTORY_NOT_FOUND, "未找到匹配的物料库存记录");
        }
        return record;
    }

    private InventoryMaterial resolveOrCreateMaterialRecord(InventoryChangeContext context, Long warehouseId) {
        InventoryMaterial record = inventoryMaterialRepository.selectByMaterialAndWarehouseAndBatch(
                context.materialId(), warehouseId, normalizeBatchNo(context.batchNo()));
        if (record != null) {
            return record;
        }

        InventoryMaterial created = new InventoryMaterial();
        created.setMaterialId(context.materialId());
        created.setWarehouseId(warehouseId);
        created.setBatchNo(normalizeBatchNo(context.batchNo()));
        created.setAvailableQty(BigDecimal.ZERO);
        created.setLockedQty(BigDecimal.ZERO);
        created.setQcQty(BigDecimal.ZERO);
        created.setTotalQty(BigDecimal.ZERO);
        created.setInTransitQty(BigDecimal.ZERO);
        inventoryMaterialRepository.insert(created);
        if (created.getId() == null) {
            throw new BizException(ErrorCode.INVENTORY_NOT_FOUND, "物料库存记录创建失败");
        }
        return created;
    }

    private Long resolveWarehouseId(InventoryChangeContext context) {
        if (context.warehouseId() != null) {
            return context.warehouseId();
        }
        return resolveInTransit(context).getWarehouseId();
    }

    private ChangeInventoryCommand buildCommand(
            OperationType operationType,
            InventoryChangeContext context,
            InventoryMaterial record) {
        ChangeInventoryCommand cmd = new ChangeInventoryCommand();
        cmd.setOperationType(operationType);
        cmd.setInventoryType(InventoryType.MATERIAL);
        cmd.setInventoryId(record.getId());
        cmd.setMaterialId(context.materialId());
        cmd.setWarehouseId(record.getWarehouseId());
        cmd.setLocationId(record.getLocationId());
        cmd.setBatchNo(record.getBatchNo());
        cmd.setQuantity(context.quantity());
        cmd.setSourceType("PROCUREMENT");
        cmd.setSourceLineId(context.procurementLineId());
        return cmd;
    }

    private void updateInTransitReceivedQty(InventoryInTransit inTransit, BigDecimal quantity) {
        BigDecimal received = nullToZero(inTransit.getReceivedQty()).add(quantity);
        BigDecimal remaining = nullToZero(inTransit.getRemainingQty()).subtract(quantity).max(BigDecimal.ZERO);
        inTransit.setReceivedQty(received);
        inTransit.setRemainingQty(remaining);
        inTransit.setStatus(remaining.compareTo(BigDecimal.ZERO) == 0
                ? InTransitStatus.FULLY_RECEIVED
                : InTransitStatus.PARTIAL_RECEIVED);
        inventoryInTransitRepository.updateById(inTransit);
    }

    private String normalizeBatchNo(String batchNo) {
        return batchNo == null ? "" : batchNo.trim();
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
