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
        Page<InventorySku> page = inventorySkuRepository.pageQuery(
                dto.getCurrent(),
                dto.getSize(),
                dto.getSkuId(),
                dto.getWarehouseId(),
                dto.getBatchNo());
        return toSkuPage(page);
    }

    public Page<InventoryMaterialVO> pageMaterials(InventoryStockQueryDTO dto) {
        Page<InventoryMaterial> page = inventoryMaterialRepository.pageQuery(
                dto.getCurrent(),
                dto.getSize(),
                dto.getMaterialId(),
                dto.getWarehouseId(),
                dto.getBatchNo());
        return toMaterialPage(page);
    }

    private Page<InventorySkuVO> toSkuPage(Page<InventorySku> source) {
        Page<InventorySkuVO> target = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        target.setRecords(source.getRecords().stream().map(this::toSkuVO).toList());
        target.setPages(source.getPages());
        return target;
    }

    private Page<InventoryMaterialVO> toMaterialPage(Page<InventoryMaterial> source) {
        Page<InventoryMaterialVO> target = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        target.setRecords(source.getRecords().stream().map(this::toMaterialVO).toList());
        target.setPages(source.getPages());
        return target;
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
