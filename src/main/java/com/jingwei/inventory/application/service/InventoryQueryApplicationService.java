package com.jingwei.inventory.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.inventory.application.dto.InventoryStockQueryDTO;
import com.jingwei.inventory.domain.model.InventoryMaterial;
import com.jingwei.inventory.domain.model.InventorySku;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import com.jingwei.inventory.interfaces.vo.InventoryMaterialVO;
import com.jingwei.inventory.interfaces.vo.InventorySkuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 库存查询应用服务
 *
 * @author JingWei
 */
@Service
@RequiredArgsConstructor
public class InventoryQueryApplicationService {

    private final InventorySkuRepository inventorySkuRepository;
    private final InventoryMaterialRepository inventoryMaterialRepository;

    public Page<InventorySkuVO> pageSkus(InventoryStockQueryDTO dto) {
        List<InventorySkuVO> records = inventorySkuRepository.selectAll().stream()
                .filter(record -> dto.getSkuId() == null || dto.getSkuId().equals(record.getSkuId()))
                .filter(record -> dto.getWarehouseId() == null || dto.getWarehouseId().equals(record.getWarehouseId()))
                .filter(record -> dto.getBatchNo() == null || dto.getBatchNo().isBlank()
                        || dto.getBatchNo().trim().equals(record.getBatchNo()))
                .map(this::toSkuVO)
                .toList();
        return toPage(records, dto.getCurrent(), dto.getSize());
    }

    public Page<InventoryMaterialVO> pageMaterials(InventoryStockQueryDTO dto) {
        List<InventoryMaterialVO> records = inventoryMaterialRepository.selectAll().stream()
                .filter(record -> dto.getMaterialId() == null || dto.getMaterialId().equals(record.getMaterialId()))
                .filter(record -> dto.getWarehouseId() == null || dto.getWarehouseId().equals(record.getWarehouseId()))
                .filter(record -> dto.getBatchNo() == null || dto.getBatchNo().isBlank()
                        || dto.getBatchNo().trim().equals(record.getBatchNo()))
                .map(this::toMaterialVO)
                .toList();
        return toPage(records, dto.getCurrent(), dto.getSize());
    }

    private <T> Page<T> toPage(List<T> records, Long current, Long size) {
        long safeCurrent = Math.max(1L, current == null ? 1L : current);
        long safeSize = Math.max(1L, size == null ? 20L : size);
        int fromIndex = (int) Math.min((safeCurrent - 1) * safeSize, records.size());
        int toIndex = (int) Math.min(fromIndex + safeSize, records.size());
        Page<T> page = new Page<>(safeCurrent, safeSize);
        page.setRecords(records.subList(fromIndex, toIndex));
        page.setTotal(records.size());
        return page;
    }

    private InventorySkuVO toSkuVO(InventorySku record) {
        InventorySkuVO vo = new InventorySkuVO();
        vo.setId(record.getId());
        vo.setSkuId(record.getSkuId());
        vo.setWarehouseId(record.getWarehouseId());
        vo.setLocationId(record.getLocationId());
        vo.setBatchNo(record.getBatchNo());
        vo.setAvailableQty(record.getAvailableQty());
        vo.setLockedQty(record.getLockedQty());
        vo.setQcQty(record.getQcQty());
        vo.setTotalQty(record.getTotalQty());
        vo.setInTransitQty(record.getInTransitQty());
        vo.setUnitCost(record.getUnitCost());
        vo.setLastInboundDate(record.getLastInboundDate());
        vo.setLastOutboundDate(record.getLastOutboundDate());
        return vo;
    }

    private InventoryMaterialVO toMaterialVO(InventoryMaterial record) {
        InventoryMaterialVO vo = new InventoryMaterialVO();
        vo.setId(record.getId());
        vo.setMaterialId(record.getMaterialId());
        vo.setWarehouseId(record.getWarehouseId());
        vo.setLocationId(record.getLocationId());
        vo.setBatchNo(record.getBatchNo());
        vo.setSupplierId(record.getSupplierId());
        vo.setProcurementOrderId(record.getProcurementOrderId());
        vo.setAvailableQty(record.getAvailableQty());
        vo.setLockedQty(record.getLockedQty());
        vo.setQcQty(record.getQcQty());
        vo.setTotalQty(record.getTotalQty());
        vo.setInTransitQty(record.getInTransitQty());
        vo.setUnitCost(record.getUnitCost());
        vo.setRollCount(record.getRollCount());
        vo.setLastInboundDate(record.getLastInboundDate());
        vo.setLastOutboundDate(record.getLastOutboundDate());
        return vo;
    }
}
