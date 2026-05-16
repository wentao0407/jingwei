package com.jingwei.inventory.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.domain.model.UserContext;
import com.jingwei.inventory.application.dto.CreateTransferDTO;
import com.jingwei.inventory.application.dto.TransferQueryDTO;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.repository.TransferOrderRepository;
import com.jingwei.inventory.domain.service.ChangeInventoryCommand;
import com.jingwei.inventory.domain.service.InventoryDomainService;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import com.jingwei.inventory.interfaces.vo.TransferOrderVO;
import com.jingwei.master.domain.model.Warehouse;
import com.jingwei.master.domain.repository.WarehouseRepository;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 调拨单应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferApplicationService {

    private static final String TRANSFER_NO_RULE = "TRANSFER_NO";

    private final TransferOrderRepository transferOrderRepository;
    private final InventoryDomainService inventoryDomainService;
    private final InventorySkuRepository inventorySkuRepository;
    private final InventoryMaterialRepository inventoryMaterialRepository;
    private final CodingRuleDomainService codingRuleDomainService;
    private final WarehouseRepository warehouseRepository;

    /**
     * 创建调拨单
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferOrderVO createTransfer(CreateTransferDTO dto) {
        if (dto.getSourceWarehouseId().equals(dto.getTargetWarehouseId())) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "源仓库和目标仓库不能相同");
        }

        String transferNo = codingRuleDomainService.generateCode(TRANSFER_NO_RULE, Collections.emptyMap());

        TransferOrder order = new TransferOrder();
        order.setTransferNo(transferNo);
        order.setSourceWarehouseId(dto.getSourceWarehouseId());
        order.setTargetWarehouseId(dto.getTargetWarehouseId());
        order.setStatus(TransferStatus.DRAFT);
        order.setRemark(dto.getRemark());
        transferOrderRepository.insert(order);

        List<TransferOrderLine> lines = new ArrayList<>();
        for (CreateTransferDTO.TransferLineDTO lineDTO : dto.getLines()) {
            TransferOrderLine line = new TransferOrderLine();
            line.setTransferId(order.getId());
            line.setInventoryType(lineDTO.getInventoryType());
            line.setSkuId(lineDTO.getSkuId());
            line.setMaterialId(lineDTO.getMaterialId());
            line.setQuantity(lineDTO.getQuantity());
            line.setBatchNo(lineDTO.getBatchNo());
            line.setRemark(lineDTO.getRemark());
            lines.add(line);
        }
        order.setLines(lines);

        log.info("创建调拨单: id={}, transferNo={}", order.getId(), transferNo);
        return toVO(order);
    }

    /**
     * 确认调拨 — 源仓可用库存扣减
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmTransfer(Long transferId) {
        TransferOrder order = transferOrderRepository.selectDetailById(transferId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND);
        }
        if (order.getStatus() != TransferStatus.DRAFT) {
            throw new BizException(ErrorCode.ORDER_STATE_TRANSITION_INVALID, "仅草稿状态可确认");
        }

        for (TransferOrderLine line : order.getLines()) {
            ChangeInventoryCommand cmd = buildChangeCommand(
                    line, order.getSourceWarehouseId(), OperationType.TRANSFER_OUT);
            inventoryDomainService.changeInventory(cmd);
        }

        order.setStatus(TransferStatus.CONFIRMED);
        transferOrderRepository.updateById(order);
        log.info("确认调拨单: id={}", transferId);
    }

    /**
     * 完成调拨 — 目标仓可用库存增加
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeTransfer(Long transferId) {
        TransferOrder order = transferOrderRepository.selectDetailById(transferId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND);
        }
        if (order.getStatus() != TransferStatus.CONFIRMED && order.getStatus() != TransferStatus.IN_TRANSIT) {
            throw new BizException(ErrorCode.ORDER_STATE_TRANSITION_INVALID, "仅已确认或在途状态可完成");
        }

        for (TransferOrderLine line : order.getLines()) {
            ChangeInventoryCommand cmd = buildChangeCommand(
                    line, order.getTargetWarehouseId(), OperationType.TRANSFER_IN);
            inventoryDomainService.changeInventory(cmd);
        }

        order.setStatus(TransferStatus.COMPLETED);
        transferOrderRepository.updateById(order);
        log.info("完成调拨单: id={}", transferId);
    }

    /**
     * 取消调拨 — 如果已确认则释放源仓扣减
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelTransfer(Long transferId) {
        TransferOrder order = transferOrderRepository.selectDetailById(transferId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND);
        }
        if (order.getStatus() == TransferStatus.COMPLETED) {
            throw new BizException(ErrorCode.ORDER_STATE_TRANSITION_INVALID, "已完成的调拨单不可取消");
        }

        // 如果已确认，需要释放源仓的扣减
        if (order.getStatus() == TransferStatus.CONFIRMED || order.getStatus() == TransferStatus.IN_TRANSIT) {
            for (TransferOrderLine line : order.getLines()) {
                ChangeInventoryCommand cmd = buildChangeCommand(
                        line, order.getSourceWarehouseId(), OperationType.TRANSFER_IN);
                inventoryDomainService.changeInventory(cmd);
            }
        }

        order.setStatus(TransferStatus.CANCELLED);
        transferOrderRepository.updateById(order);
        log.info("取消调拨单: id={}", transferId);
    }

    /**
     * 查询详情
     */
    public TransferOrderVO getDetail(Long transferId) {
        TransferOrder order = transferOrderRepository.selectDetailById(transferId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND);
        }
        return toVO(order);
    }

    /**
     * 分页查询
     */
    public IPage<TransferOrderVO> pageQuery(TransferQueryDTO dto) {
        Page<TransferOrder> page = new Page<>(dto.getCurrent(), dto.getSize());
        TransferStatus status = dto.getStatus() != null ? TransferStatus.valueOf(dto.getStatus()) : null;
        IPage<TransferOrder> orderPage = transferOrderRepository.selectPage(page, status);
        return orderPage.convert(this::toVO);
    }

    private ChangeInventoryCommand buildChangeCommand(TransferOrderLine line, Long warehouseId, OperationType opType) {
        ChangeInventoryCommand cmd = new ChangeInventoryCommand();
        cmd.setOperationType(opType);
        cmd.setWarehouseId(warehouseId);
        cmd.setQuantity(line.getQuantity());

        if ("SKU".equals(line.getInventoryType())) {
            cmd.setInventoryType(InventoryType.SKU);
            cmd.setSkuId(line.getSkuId());
            List<InventorySku> skus = inventorySkuRepository.selectBySkuAndWarehouse(line.getSkuId(), warehouseId);
            if (skus != null && !skus.isEmpty()) {
                cmd.setInventoryId(skus.get(0).getId());
            }
        } else {
            cmd.setInventoryType(InventoryType.MATERIAL);
            cmd.setMaterialId(line.getMaterialId());
            List<InventoryMaterial> mats = inventoryMaterialRepository.selectByMaterialAndWarehouse(line.getMaterialId(), warehouseId);
            if (mats != null && !mats.isEmpty()) {
                cmd.setInventoryId(mats.get(0).getId());
            }
        }

        return cmd;
    }

    private TransferOrderVO toVO(TransferOrder order) {
        TransferOrderVO vo = new TransferOrderVO();
        vo.setId(order.getId());
        vo.setTransferNo(order.getTransferNo());
        vo.setSourceWarehouseId(order.getSourceWarehouseId());
        vo.setTargetWarehouseId(order.getTargetWarehouseId());
        vo.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        vo.setStatusLabel(order.getStatus() != null ? order.getStatus().getLabel() : null);
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());

        // 补充仓库名称
        if (order.getSourceWarehouseId() != null) {
            Warehouse src = warehouseRepository.selectById(order.getSourceWarehouseId());
            if (src != null) vo.setSourceWarehouseName(src.getName());
        }
        if (order.getTargetWarehouseId() != null) {
            Warehouse tgt = warehouseRepository.selectById(order.getTargetWarehouseId());
            if (tgt != null) vo.setTargetWarehouseName(tgt.getName());
        }

        if (order.getLines() != null) {
            vo.setLines(order.getLines().stream().map(this::toLineVO).toList());
        } else {
            vo.setLines(List.of());
        }
        return vo;
    }

    private TransferOrderVO.TransferOrderLineVO toLineVO(TransferOrderLine line) {
        TransferOrderVO.TransferOrderLineVO vo = new TransferOrderVO.TransferOrderLineVO();
        vo.setId(line.getId());
        vo.setInventoryType(line.getInventoryType());
        vo.setSkuId(line.getSkuId());
        vo.setMaterialId(line.getMaterialId());
        vo.setQuantity(line.getQuantity());
        vo.setBatchNo(line.getBatchNo());
        vo.setRemark(line.getRemark());
        return vo;
    }
}
