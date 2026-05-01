package com.jingwei.master.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.master.application.dto.*;
import com.jingwei.master.domain.model.*;
import com.jingwei.master.domain.service.WarehouseDomainService;
import com.jingwei.master.interfaces.vo.LocationVO;
import com.jingwei.master.interfaces.vo.WarehouseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 仓库应用服务
 * <p>
 * 负责仓库和库位 CRUD 的编排和事务边界管理。
 * 业务校验委托给 WarehouseDomainService。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseApplicationService {

    private final WarehouseDomainService warehouseDomainService;

    // ==================== 仓库操作 ====================

    @Transactional(rollbackFor = Exception.class)
    public WarehouseVO createWarehouse(CreateWarehouseDTO dto) {
        Warehouse warehouse = new Warehouse();
        warehouse.setCode(dto.getCode());
        warehouse.setName(dto.getName());
        warehouse.setType(WarehouseType.valueOf(dto.getType()));
        warehouse.setAddress(dto.getAddress());
        warehouse.setManagerId(dto.getManagerId());
        warehouse.setRemark(dto.getRemark() != null ? dto.getRemark() : "");

        Warehouse saved = warehouseDomainService.createWarehouse(warehouse);
        return toWarehouseVO(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public WarehouseVO updateWarehouse(Long warehouseId, UpdateWarehouseDTO dto) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(dto.getName());
        warehouse.setAddress(dto.getAddress());
        warehouse.setManagerId(dto.getManagerId());
        warehouse.setRemark(dto.getRemark());

        Warehouse updated = warehouseDomainService.updateWarehouse(warehouseId, warehouse);
        return toWarehouseVO(updated);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deactivateWarehouse(Long warehouseId) {
        warehouseDomainService.deactivateWarehouse(warehouseId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void activateWarehouse(Long warehouseId) {
        warehouseDomainService.activateWarehouse(warehouseId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteWarehouse(Long warehouseId) {
        warehouseDomainService.deleteWarehouse(warehouseId);
    }

    public WarehouseVO getWarehouseById(Long warehouseId) {
        Warehouse warehouse = warehouseDomainService.getWarehouseById(warehouseId);
        return toWarehouseVO(warehouse);
    }

    public WarehouseVO getWarehouseDetail(Long warehouseId) {
        Warehouse warehouse = warehouseDomainService.getWarehouseDetail(warehouseId);
        return toWarehouseVOWithLocations(warehouse);
    }

    public List<WarehouseVO> listWarehouses(String type, String status) {
        List<Warehouse> warehouses = warehouseDomainService.listWarehouses(type, status);
        return warehouses.stream().map(this::toWarehouseVO).toList();
    }

    public IPage<WarehouseVO> pageQuery(WarehouseQueryDTO dto) {
        Page<Warehouse> page = new Page<>(dto.getCurrent(), dto.getSize());
        IPage<Warehouse> warehousePage = warehouseDomainService.getWarehouseRepository()
                .selectPage(page, dto.getType(), dto.getStatus(), dto.getKeyword());
        return warehousePage.convert(this::toWarehouseVO);
    }

    // ==================== 库位操作 ====================

    @Transactional(rollbackFor = Exception.class)
    public LocationVO createLocation(Long warehouseId, CreateLocationDTO dto) {
        Location location = new Location();
        location.setZoneCode(dto.getZoneCode());
        location.setRackCode(dto.getRackCode());
        location.setRowCode(dto.getRowCode());
        location.setBinCode(dto.getBinCode());
        location.setLocationType(LocationType.valueOf(dto.getLocationType()));
        location.setCapacity(dto.getCapacity());
        location.setRemark(dto.getRemark() != null ? dto.getRemark() : "");

        Location saved = warehouseDomainService.createLocation(warehouseId, location);
        return toLocationVO(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public LocationVO updateLocation(Long locationId, UpdateLocationDTO dto) {
        Location location = new Location();
        location.setCapacity(dto.getCapacity());
        location.setRemark(dto.getRemark());

        Location updated = warehouseDomainService.updateLocation(locationId, location);
        return toLocationVO(updated);
    }

    @Transactional(rollbackFor = Exception.class)
    public void freezeLocation(Long locationId) {
        warehouseDomainService.freezeLocation(locationId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unfreezeLocation(Long locationId) {
        warehouseDomainService.unfreezeLocation(locationId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deactivateLocation(Long locationId) {
        warehouseDomainService.deactivateLocation(locationId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteLocation(Long locationId) {
        warehouseDomainService.deleteLocation(locationId);
    }

    // ==================== 转换方法 ====================

    private WarehouseVO toWarehouseVO(Warehouse warehouse) {
        WarehouseVO vo = new WarehouseVO();
        vo.setId(warehouse.getId());
        vo.setCode(warehouse.getCode());
        vo.setName(warehouse.getName());
        vo.setType(warehouse.getType() != null ? warehouse.getType().name() : null);
        vo.setAddress(warehouse.getAddress());
        vo.setManagerId(warehouse.getManagerId());
        vo.setStatus(warehouse.getStatus() != null ? warehouse.getStatus().name() : null);
        vo.setRemark(warehouse.getRemark());
        vo.setCreatedAt(warehouse.getCreatedAt());
        vo.setUpdatedAt(warehouse.getUpdatedAt());
        return vo;
    }

    private WarehouseVO toWarehouseVOWithLocations(Warehouse warehouse) {
        WarehouseVO vo = toWarehouseVO(warehouse);
        if (warehouse.getLocations() != null && !warehouse.getLocations().isEmpty()) {
            vo.setLocations(warehouse.getLocations().stream().map(this::toLocationVO).toList());
        }
        return vo;
    }

    private LocationVO toLocationVO(Location location) {
        LocationVO vo = new LocationVO();
        vo.setId(location.getId());
        vo.setWarehouseId(location.getWarehouseId());
        vo.setZoneCode(location.getZoneCode());
        vo.setRackCode(location.getRackCode());
        vo.setRowCode(location.getRowCode());
        vo.setBinCode(location.getBinCode());
        vo.setFullCode(location.getFullCode());
        vo.setLocationType(location.getLocationType() != null ? location.getLocationType().name() : null);
        vo.setCapacity(location.getCapacity());
        vo.setUsedCapacity(location.getUsedCapacity());
        vo.setStatus(location.getStatus() != null ? location.getStatus().name() : null);
        vo.setRemark(location.getRemark());
        vo.setCreatedAt(location.getCreatedAt());
        vo.setUpdatedAt(location.getUpdatedAt());
        return vo;
    }
}
