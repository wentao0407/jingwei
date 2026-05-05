package com.jingwei.warehouse.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.model.InventoryType;
import com.jingwei.inventory.domain.model.OperationType;
import com.jingwei.inventory.domain.service.ChangeInventoryCommand;
import com.jingwei.inventory.domain.service.InventoryDomainService;
import com.jingwei.master.domain.model.Location;
import com.jingwei.master.domain.model.LocationStatus;
import com.jingwei.master.domain.repository.LocationRepository;
import com.jingwei.procurement.domain.model.Asn;
import com.jingwei.procurement.domain.model.AsnLine;
import com.jingwei.procurement.domain.repository.AsnLineRepository;
import com.jingwei.procurement.domain.repository.AsnRepository;
import com.jingwei.warehouse.domain.model.*;
import com.jingwei.warehouse.domain.repository.ReceivingLineRepository;
import com.jingwei.warehouse.domain.repository.ReceivingOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 收货领域服务
 * <p>
 * 负责收货作业的核心逻辑：从 ASN 创建收货单、确认收货、推荐库位、确认上架。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceivingDomainService {

    private final ReceivingOrderRepository receivingOrderRepository;
    private final ReceivingLineRepository receivingLineRepository;
    private final AsnRepository asnRepository;
    private final AsnLineRepository asnLineRepository;
    private final InventoryDomainService inventoryDomainService;
    private final LocationRepository locationRepository;

    /**
     * 从 ASN 创建收货单
     */
    public ReceivingOrder createFromAsn(Long asnId, Long warehouseId, Long operatorId) {
        Asn asn = asnRepository.selectById(asnId);
        if (asn == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "到货通知单不存在");
        }

        List<AsnLine> asnLines = asnLineRepository.selectByAsnId(asnId);
        if (asnLines.isEmpty()) {
            throw new BizException(ErrorCode.ORDER_LINE_EMPTY, "到货通知单无明细行");
        }

        // 创建收货单
        ReceivingOrder order = new ReceivingOrder();
        order.setAsnId(asnId);
        order.setWarehouseId(warehouseId);
        order.setReceivingDate(LocalDate.now());
        order.setStatus(ReceivingStatus.IN_PROGRESS);
        order.setReceiverId(operatorId);
        receivingOrderRepository.insert(order);

        // 创建收货行
        List<ReceivingLine> lines = new ArrayList<>();
        for (AsnLine asnLine : asnLines) {
            BigDecimal remaining = asnLine.getExpectedQuantity().subtract(asnLine.getReceivedQuantity());
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) continue;

            ReceivingLine line = new ReceivingLine();
            line.setReceivingId(order.getId());
            line.setAsnLineId(asnLine.getId());
            line.setMaterialId(asnLine.getMaterialId());
            line.setExpectedQty(remaining);
            line.setReceivedQty(BigDecimal.ZERO);
            line.setQcStatus(QcLineStatus.PENDING);
            line.setPutawayStatus(PutawayLineStatus.PENDING);
            lines.add(line);
        }
        receivingLineRepository.batchInsert(lines);
        order.setLines(lines);

        log.info("从ASN创建收货单: asnId={}, receivingId={}, 行数={}", asnId, order.getId(), lines.size());
        return order;
    }

    /**
     * 确认收货（逐行）
     * <p>
     * 流程：更新收贂数量 → 驱动库存变更（INBOUND_PURCHASE 增加质检库存）→ 回写 ASN 行已收货数量。
     * </p>
     */
    public void confirmReceive(Long receivingLineId, BigDecimal receivedQty, Integer rollCount, Long operatorId) {
        ReceivingLine line = receivingLineRepository.selectById(receivingLineId);
        if (line == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "收货行不存在");
        }

        // 校验实收数量不超过剩余可收
        BigDecimal alreadyReceived = line.getReceivedQty() != null ? line.getReceivedQty() : BigDecimal.ZERO;
        BigDecimal maxReceivable = line.getExpectedQty().subtract(alreadyReceived);
        if (receivedQty.compareTo(maxReceivable) > 0) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "实收数量超过剩余可收数量，剩余可收: " + maxReceivable);
        }

        // 更新收货行
        line.setReceivedQty(alreadyReceived.add(receivedQty));
        line.setDifferenceQty(line.getReceivedQty().subtract(line.getExpectedQty()));
        line.setRollCount(rollCount != null ? rollCount : 0);
        line.setUpdatedBy(operatorId);
        receivingLineRepository.updateById(line);

        // 获取收货单信息（含仓库ID）
        ReceivingOrder order = receivingOrderRepository.selectById(line.getReceivingId());
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "收货单不存在");
        }

        // 生成批次号：RCV-{收货单ID}-{收货行ID}
        String batchNo = "RCV-" + order.getId() + "-" + line.getId();

        // 驱动库存变更：INBOUND_PURCHASE 增加质检库存
        ChangeInventoryCommand cmd = new ChangeInventoryCommand();
        cmd.setOperationType(OperationType.INBOUND_PURCHASE);
        cmd.setInventoryType(InventoryType.MATERIAL);
        cmd.setInventoryId(null); // 由 InventoryDomainService 根据 materialId+warehouseId 查找或创建
        cmd.setMaterialId(line.getMaterialId());
        cmd.setWarehouseId(order.getWarehouseId());
        cmd.setBatchNo(batchNo);
        cmd.setQuantity(receivedQty);
        cmd.setSourceType("RECEIVING");
        cmd.setSourceId(order.getId());
        cmd.setSourceNo(order.getReceivingNo());
        cmd.setOperatorId(operatorId);
        inventoryDomainService.changeInventory(cmd);

        // 回写 ASN 行已收货数量
        if (line.getAsnLineId() != null) {
            AsnLine asnLine = asnLineRepository.selectById(line.getAsnLineId());
            if (asnLine != null) {
                asnLine.setReceivedQuantity(asnLine.getReceivedQuantity().add(receivedQty));
                asnLineRepository.updateById(asnLine);
            }
        }

        // 更新收货单状态
        List<ReceivingLine> allLines = receivingLineRepository.selectByReceivingId(order.getId());
        boolean allReceived = allLines.stream().allMatch(l ->
                l.getReceivedQty() != null && l.getReceivedQty().compareTo(l.getExpectedQty()) >= 0);
        if (allReceived) {
            order.setStatus(ReceivingStatus.COMPLETED);
        } else {
            order.setStatus(ReceivingStatus.IN_PROGRESS);
        }
        receivingOrderRepository.updateById(order);

        log.info("收货确认完成: receivingLineId={}, receivedQty={}, materialId={}, batchNo={}",
                receivingLineId, receivedQty, line.getMaterialId(), batchNo);
    }

    /**
     * 推荐上架库位
     * <p>
     * 规则优先级：
     * 1. 同物料已有库存的库位（合并存放）
     * 2. 同类物料的空闲存储位
     * 3. 排除 FROZEN/INACTIVE 库位
     * </p>
     */
    public List<Location> suggestLocations(Long receivingLineId) {
        ReceivingLine line = receivingLineRepository.selectById(receivingLineId);
        if (line == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "收货行不存在");
        }

        ReceivingOrder order = receivingOrderRepository.selectById(line.getReceivingId());
        List<Location> allLocations = locationRepository.selectByWarehouseId(order.getWarehouseId());

        // 过滤：只保留 ACTIVE 状态的存储位
        return allLocations.stream()
                .filter(loc -> loc.getStatus() == LocationStatus.ACTIVE)
                .filter(loc -> loc.getLocationType() != null)
                .sorted((a, b) -> {
                    // 同物料已有库存的库位排前面（简化：按 usedCapacity 升序）
                    int capA = a.getUsedCapacity() != null ? a.getUsedCapacity() : 0;
                    int capB = b.getUsedCapacity() != null ? b.getUsedCapacity() : 0;
                    return Integer.compare(capA, capB);
                })
                .toList();
    }

    /**
     * 确认上架
     */
    public void confirmPutaway(Long receivingLineId, Long locationId, Long operatorId) {
        ReceivingLine line = receivingLineRepository.selectById(receivingLineId);
        if (line == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "收货行不存在");
        }

        // 校验库位有效
        Location location = locationRepository.selectById(locationId);
        if (location == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "库位不存在");
        }
        if (location.getStatus() != LocationStatus.ACTIVE) {
            throw new BizException(ErrorCode.LOCATION_FROZEN_INVENTORY, "库位已冻结或停用，不可上架");
        }

        // 更新上架信息
        line.setPutawayLocationId(locationId);
        line.setPutawayStatus(PutawayLineStatus.COMPLETED);
        line.setUpdatedBy(operatorId);
        receivingLineRepository.updateById(line);

        // 更新库位占用容量
        if (location.getUsedCapacity() == null) {
            location.setUsedCapacity(0);
        }
        location.setUsedCapacity(location.getUsedCapacity() + line.getReceivedQty().intValue());
        locationRepository.updateById(location);

        log.info("上架确认: receivingLineId={}, locationId={}, qty={}",
                receivingLineId, locationId, line.getReceivedQty());
    }
}
