package com.jingwei.master.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.master.application.dto.*;
import com.jingwei.master.application.service.WarehouseApplicationService;
import com.jingwei.master.interfaces.vo.LocationVO;
import com.jingwei.master.interfaces.vo.WarehouseVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仓库管理 Controller
 * <p>
 * 提供仓库和库位的 CRUD 接口。
 * 库位从属于仓库，库位操作通过 /master/warehouse/location 系列路径访问。
 * 所有接口统一使用 POST 方法。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseApplicationService warehouseApplicationService;

    // ==================== 仓库接口 ====================

    @RequirePermission("master:warehouse:create")
    @PostMapping("/master/warehouse/create")
    public R<WarehouseVO> createWarehouse(@Valid @RequestBody CreateWarehouseDTO dto) {
        return R.ok(warehouseApplicationService.createWarehouse(dto));
    }

    @RequirePermission("master:warehouse:update")
    @PostMapping("/master/warehouse/update")
    public R<WarehouseVO> updateWarehouse(@RequestParam Long warehouseId,
                                          @Valid @RequestBody UpdateWarehouseDTO dto) {
        return R.ok(warehouseApplicationService.updateWarehouse(warehouseId, dto));
    }

    @RequirePermission("master:warehouse:deactivate")
    @PostMapping("/master/warehouse/deactivate")
    public R<Void> deactivateWarehouse(@RequestParam Long warehouseId) {
        warehouseApplicationService.deactivateWarehouse(warehouseId);
        return R.ok();
    }

    @RequirePermission("master:warehouse:activate")
    @PostMapping("/master/warehouse/activate")
    public R<Void> activateWarehouse(@RequestParam Long warehouseId) {
        warehouseApplicationService.activateWarehouse(warehouseId);
        return R.ok();
    }

    @RequirePermission("master:warehouse:delete")
    @PostMapping("/master/warehouse/delete")
    public R<Void> deleteWarehouse(@RequestParam Long warehouseId) {
        warehouseApplicationService.deleteWarehouse(warehouseId);
        return R.ok();
    }

    @PostMapping("/master/warehouse/detail")
    public R<WarehouseVO> getWarehouseDetail(@RequestParam Long warehouseId) {
        return R.ok(warehouseApplicationService.getWarehouseDetail(warehouseId));
    }

    @PostMapping("/master/warehouse/list")
    public R<List<WarehouseVO>> listWarehouses(@RequestParam(required = false) String type,
                                                @RequestParam(required = false) String status) {
        return R.ok(warehouseApplicationService.listWarehouses(type, status));
    }

    @PostMapping("/master/warehouse/page")
    public R<IPage<WarehouseVO>> pageQuery(@Valid @RequestBody WarehouseQueryDTO dto) {
        return R.ok(warehouseApplicationService.pageQuery(dto));
    }

    // ==================== 库位接口 ====================

    @RequirePermission("master:location:create")
    @PostMapping("/master/warehouse/location/create")
    public R<LocationVO> createLocation(@RequestParam Long warehouseId,
                                        @Valid @RequestBody CreateLocationDTO dto) {
        return R.ok(warehouseApplicationService.createLocation(warehouseId, dto));
    }

    @RequirePermission("master:location:update")
    @PostMapping("/master/warehouse/location/update")
    public R<LocationVO> updateLocation(@RequestParam Long locationId,
                                        @Valid @RequestBody UpdateLocationDTO dto) {
        return R.ok(warehouseApplicationService.updateLocation(locationId, dto));
    }

    @RequirePermission("master:location:freeze")
    @PostMapping("/master/warehouse/location/freeze")
    public R<Void> freezeLocation(@RequestParam Long locationId) {
        warehouseApplicationService.freezeLocation(locationId);
        return R.ok();
    }

    @RequirePermission("master:location:unfreeze")
    @PostMapping("/master/warehouse/location/unfreeze")
    public R<Void> unfreezeLocation(@RequestParam Long locationId) {
        warehouseApplicationService.unfreezeLocation(locationId);
        return R.ok();
    }

    @RequirePermission("master:location:deactivate")
    @PostMapping("/master/warehouse/location/deactivate")
    public R<Void> deactivateLocation(@RequestParam Long locationId) {
        warehouseApplicationService.deactivateLocation(locationId);
        return R.ok();
    }

    @RequirePermission("master:location:delete")
    @PostMapping("/master/warehouse/location/delete")
    public R<Void> deleteLocation(@RequestParam Long locationId) {
        warehouseApplicationService.deleteLocation(locationId);
        return R.ok();
    }
}
