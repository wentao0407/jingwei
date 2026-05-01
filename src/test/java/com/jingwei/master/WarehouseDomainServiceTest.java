package com.jingwei.master;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.CommonStatus;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.*;
import com.jingwei.master.domain.repository.LocationRepository;
import com.jingwei.master.domain.repository.WarehouseRepository;
import com.jingwei.master.domain.service.WarehouseDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WarehouseDomainService 单元测试
 * <p>
 * 分为仓库和库位两组测试，覆盖核心业务规则。
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class WarehouseDomainServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private WarehouseDomainService warehouseDomainService;

    // ==================== 仓库测试 ====================

    @Nested
    @DisplayName("仓库 CRUD")
    class WarehouseTests {

        @Test
        @DisplayName("创建仓库 — 默认 ACTIVE")
        void createWarehouse_shouldSetDefaultStatus() {
            when(warehouseRepository.existsByCode("WH01", null)).thenReturn(false);
            when(warehouseRepository.insert(any())).thenReturn(1);

            Warehouse warehouse = buildWarehouse("WH01", "成品仓", WarehouseType.FINISHED_GOODS);
            Warehouse result = warehouseDomainService.createWarehouse(warehouse);

            assertEquals(CommonStatus.ACTIVE, result.getStatus());
        }

        @Test
        @DisplayName("创建仓库 — 编码为空应抛异常")
        void createWarehouse_codeBlank_shouldThrow() {
            Warehouse warehouse = buildWarehouse("", "成品仓", WarehouseType.FINISHED_GOODS);
            assertThrows(BizException.class, () -> warehouseDomainService.createWarehouse(warehouse));
        }

        @Test
        @DisplayName("创建仓库 — 编码重复应抛异常")
        void createWarehouse_duplicateCode_shouldThrow() {
            when(warehouseRepository.existsByCode("WH01", null)).thenReturn(true);

            Warehouse warehouse = buildWarehouse("WH01", "成品仓", WarehouseType.FINISHED_GOODS);
            BizException ex = assertThrows(BizException.class,
                    () -> warehouseDomainService.createWarehouse(warehouse));
            assertTrue(ex.getMessage().contains("仓库编码已存在"));
        }

        @Test
        @DisplayName("创建仓库 — 并发唯一约束冲突应抛异常")
        void createWarehouse_concurrentDuplicate_shouldThrow() {
            when(warehouseRepository.existsByCode("WH01", null)).thenReturn(false);
            when(warehouseRepository.insert(any())).thenThrow(new DuplicateKeyException("uk"));

            Warehouse warehouse = buildWarehouse("WH01", "成品仓", WarehouseType.FINISHED_GOODS);
            BizException ex = assertThrows(BizException.class,
                    () -> warehouseDomainService.createWarehouse(warehouse));
            assertTrue(ex.getMessage().contains("仓库编码已存在"));
        }

        @Test
        @DisplayName("更新仓库 — 编码和类型不可修改")
        void updateWarehouse_codeAndTypeImmutable() {
            Warehouse existing = buildSavedWarehouse(1L, "WH01", "成品仓", WarehouseType.FINISHED_GOODS);
            when(warehouseRepository.selectById(1L)).thenReturn(existing);
            when(warehouseRepository.updateById(any())).thenReturn(1);

            Warehouse update = new Warehouse();
            update.setName("成品仓（更新）");

            warehouseDomainService.updateWarehouse(1L, update);

            verify(warehouseRepository).updateById(argThat(w ->
                    w.getCode().equals("WH01") && w.getType() == WarehouseType.FINISHED_GOODS));
        }

        @Test
        @DisplayName("停用仓库 — 成功")
        void deactivateWarehouse_shouldSucceed() {
            Warehouse existing = buildSavedWarehouse(1L, "WH01", "成品仓", WarehouseType.FINISHED_GOODS);
            when(warehouseRepository.selectById(1L)).thenReturn(existing);
            when(warehouseRepository.updateById(any())).thenReturn(1);

            warehouseDomainService.deactivateWarehouse(1L);
            assertEquals(CommonStatus.INACTIVE, existing.getStatus());
        }

        @Test
        @DisplayName("停用仓库 — 已停用再停用应抛异常")
        void deactivateWarehouse_alreadyInactive_shouldThrow() {
            Warehouse existing = buildSavedWarehouse(1L, "WH01", "成品仓", WarehouseType.FINISHED_GOODS);
            existing.setStatus(CommonStatus.INACTIVE);
            when(warehouseRepository.selectById(1L)).thenReturn(existing);

            BizException ex = assertThrows(BizException.class,
                    () -> warehouseDomainService.deactivateWarehouse(1L));
            assertTrue(ex.getMessage().contains("仓库已停用"));
        }

        @Test
        @DisplayName("启用仓库 — 成功")
        void activateWarehouse_shouldSucceed() {
            Warehouse existing = buildSavedWarehouse(1L, "WH01", "成品仓", WarehouseType.FINISHED_GOODS);
            existing.setStatus(CommonStatus.INACTIVE);
            when(warehouseRepository.selectById(1L)).thenReturn(existing);
            when(warehouseRepository.updateById(any())).thenReturn(1);

            warehouseDomainService.activateWarehouse(1L);
            assertEquals(CommonStatus.ACTIVE, existing.getStatus());
        }

        @Test
        @DisplayName("删除仓库 — 无库位应成功")
        void deleteWarehouse_noLocations_shouldSucceed() {
            Warehouse existing = buildSavedWarehouse(1L, "WH01", "成品仓", WarehouseType.FINISHED_GOODS);
            when(warehouseRepository.selectById(1L)).thenReturn(existing);
            when(locationRepository.selectByWarehouseId(1L)).thenReturn(List.of());
            when(warehouseRepository.deleteById(1L)).thenReturn(1);

            assertDoesNotThrow(() -> warehouseDomainService.deleteWarehouse(1L));
            verify(warehouseRepository).deleteById(1L);
        }

        @Test
        @DisplayName("删除仓库 — 同时删除其下库位")
        void deleteWarehouse_shouldAlsoDeleteLocations() {
            Warehouse existing = buildSavedWarehouse(1L, "WH01", "成品仓", WarehouseType.FINISHED_GOODS);
            Location loc1 = buildLocation(10L, 1L, "A", "01", "02", "03");
            Location loc2 = buildLocation(11L, 1L, "A", "01", "02", "04");
            when(warehouseRepository.selectById(1L)).thenReturn(existing);
            when(locationRepository.selectByWarehouseId(1L)).thenReturn(List.of(loc1, loc2));
            when(warehouseRepository.deleteById(1L)).thenReturn(1);

            warehouseDomainService.deleteWarehouse(1L);

            verify(locationRepository).deleteById(10L);
            verify(locationRepository).deleteById(11L);
            verify(warehouseRepository).deleteById(1L);
        }
    }

    // ==================== 库位测试 ====================

    @Nested
    @DisplayName("库位 CRUD")
    class LocationTests {

        @Test
        @DisplayName("创建库位 — 自动拼接 fullCode")
        void createLocation_shouldBuildFullCode() {
            Warehouse warehouse = buildSavedWarehouse(1L, "WH01", "成品仓", WarehouseType.FINISHED_GOODS);
            when(warehouseRepository.selectById(1L)).thenReturn(warehouse);
            when(locationRepository.existsByWarehouseIdAndFullCode(1L, "WH01-A-01-02-03", null)).thenReturn(false);
            when(locationRepository.insert(any())).thenReturn(1);

            Location location = new Location();
            location.setZoneCode("A");
            location.setRackCode("01");
            location.setRowCode("02");
            location.setBinCode("03");
            location.setLocationType(LocationType.STORAGE);

            Location result = warehouseDomainService.createLocation(1L, location);

            assertEquals("WH01-A-01-02-03", result.getFullCode());
            assertEquals(LocationStatus.ACTIVE, result.getStatus());
            assertEquals(0, result.getUsedCapacity());
        }

        @Test
        @DisplayName("创建库位 — 仓库不存在应抛异常")
        void createLocation_warehouseNotFound_shouldThrow() {
            when(warehouseRepository.selectById(999L)).thenReturn(null);

            Location location = new Location();
            location.setZoneCode("A");
            location.setRackCode("01");
            location.setRowCode("02");
            location.setBinCode("03");

            BizException ex = assertThrows(BizException.class,
                    () -> warehouseDomainService.createLocation(999L, location));
            assertTrue(ex.getMessage().contains("仓库不存在"));
        }

        @Test
        @DisplayName("创建库位 — 停用仓库不允许新增")
        void createLocation_warehouseInactive_shouldThrow() {
            Warehouse warehouse = buildSavedWarehouse(1L, "WH01", "成品仓", WarehouseType.FINISHED_GOODS);
            warehouse.setStatus(CommonStatus.INACTIVE);
            when(warehouseRepository.selectById(1L)).thenReturn(warehouse);

            Location location = new Location();
            location.setZoneCode("A");
            location.setRackCode("01");
            location.setRowCode("02");
            location.setBinCode("03");

            BizException ex = assertThrows(BizException.class,
                    () -> warehouseDomainService.createLocation(1L, location));
            assertTrue(ex.getMessage().contains("已停用的仓库不允许新增库位"));
        }

        @Test
        @DisplayName("创建库位 — fullCode 重复应抛异常")
        void createLocation_duplicateFullCode_shouldThrow() {
            Warehouse warehouse = buildSavedWarehouse(1L, "WH01", "成品仓", WarehouseType.FINISHED_GOODS);
            when(warehouseRepository.selectById(1L)).thenReturn(warehouse);
            when(locationRepository.existsByWarehouseIdAndFullCode(1L, "WH01-A-01-02-03", null)).thenReturn(true);

            Location location = new Location();
            location.setZoneCode("A");
            location.setRackCode("01");
            location.setRowCode("02");
            location.setBinCode("03");

            BizException ex = assertThrows(BizException.class,
                    () -> warehouseDomainService.createLocation(1L, location));
            assertTrue(ex.getMessage().contains("相同编码的库位"));
        }

        @Test
        @DisplayName("更新库位 — 冻结状态不允许修改")
        void updateLocation_frozen_shouldThrow() {
            Location existing = buildLocation(10L, 1L, "A", "01", "02", "03");
            existing.setStatus(LocationStatus.FROZEN);
            when(locationRepository.selectById(10L)).thenReturn(existing);

            Location update = new Location();
            update.setCapacity(100);

            BizException ex = assertThrows(BizException.class,
                    () -> warehouseDomainService.updateLocation(10L, update));
            assertEquals(ErrorCode.LOCATION_FROZEN.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("更新库位 — 编码和类型不可修改")
        void updateLocation_codeAndTypeImmutable() {
            Location existing = buildLocation(10L, 1L, "A", "01", "02", "03");
            existing.setLocationType(LocationType.STORAGE);
            when(locationRepository.selectById(10L)).thenReturn(existing);
            when(locationRepository.updateById(any())).thenReturn(1);

            Location update = new Location();
            update.setCapacity(100);

            warehouseDomainService.updateLocation(10L, update);

            verify(locationRepository).updateById(argThat(l ->
                    l.getFullCode().equals("WH01-A-01-02-03") &&
                    l.getLocationType() == LocationType.STORAGE &&
                    l.getZoneCode().equals("A")));
        }

        @Test
        @DisplayName("冻结库位 — 成功")
        void freezeLocation_shouldSucceed() {
            Location existing = buildLocation(10L, 1L, "A", "01", "02", "03");
            when(locationRepository.selectById(10L)).thenReturn(existing);
            when(locationRepository.updateById(any())).thenReturn(1);

            warehouseDomainService.freezeLocation(10L);
            assertEquals(LocationStatus.FROZEN, existing.getStatus());
        }

        @Test
        @DisplayName("冻结库位 — 已冻结再冻结应抛异常")
        void freezeLocation_alreadyFrozen_shouldThrow() {
            Location existing = buildLocation(10L, 1L, "A", "01", "02", "03");
            existing.setStatus(LocationStatus.FROZEN);
            when(locationRepository.selectById(10L)).thenReturn(existing);

            BizException ex = assertThrows(BizException.class,
                    () -> warehouseDomainService.freezeLocation(10L));
            assertTrue(ex.getMessage().contains("库位已冻结"));
        }

        @Test
        @DisplayName("解冻库位 — 成功")
        void unfreezeLocation_shouldSucceed() {
            Location existing = buildLocation(10L, 1L, "A", "01", "02", "03");
            existing.setStatus(LocationStatus.FROZEN);
            when(locationRepository.selectById(10L)).thenReturn(existing);
            when(locationRepository.updateById(any())).thenReturn(1);

            warehouseDomainService.unfreezeLocation(10L);
            assertEquals(LocationStatus.ACTIVE, existing.getStatus());
        }

        @Test
        @DisplayName("解冻库位 — 未冻结应抛异常")
        void unfreezeLocation_notFrozen_shouldThrow() {
            Location existing = buildLocation(10L, 1L, "A", "01", "02", "03");
            when(locationRepository.selectById(10L)).thenReturn(existing);

            BizException ex = assertThrows(BizException.class,
                    () -> warehouseDomainService.unfreezeLocation(10L));
            assertTrue(ex.getMessage().contains("库位未冻结"));
        }

        @Test
        @DisplayName("停用库位 — 冻结状态不允许停用")
        void deactivateLocation_frozen_shouldThrow() {
            Location existing = buildLocation(10L, 1L, "A", "01", "02", "03");
            existing.setStatus(LocationStatus.FROZEN);
            when(locationRepository.selectById(10L)).thenReturn(existing);

            BizException ex = assertThrows(BizException.class,
                    () -> warehouseDomainService.deactivateLocation(10L));
            assertTrue(ex.getMessage().contains("请先解冻"));
        }

        @Test
        @DisplayName("删除库位 — 冻结状态不允许删除")
        void deleteLocation_frozen_shouldThrow() {
            Location existing = buildLocation(10L, 1L, "A", "01", "02", "03");
            existing.setStatus(LocationStatus.FROZEN);
            when(locationRepository.selectById(10L)).thenReturn(existing);

            BizException ex = assertThrows(BizException.class,
                    () -> warehouseDomainService.deleteLocation(10L));
            assertEquals(ErrorCode.LOCATION_FROZEN.getCode(), ex.getCode());
        }

        @Test
        @DisplayName("删除库位 — 非冻结状态应成功")
        void deleteLocation_active_shouldSucceed() {
            Location existing = buildLocation(10L, 1L, "A", "01", "02", "03");
            when(locationRepository.selectById(10L)).thenReturn(existing);
            when(locationRepository.deleteById(10L)).thenReturn(1);

            assertDoesNotThrow(() -> warehouseDomainService.deleteLocation(10L));
            verify(locationRepository).deleteById(10L);
        }
    }

    // ==================== 辅助方法 ====================

    private Warehouse buildWarehouse(String code, String name, WarehouseType type) {
        Warehouse warehouse = new Warehouse();
        warehouse.setCode(code);
        warehouse.setName(name);
        warehouse.setType(type);
        return warehouse;
    }

    private Warehouse buildSavedWarehouse(Long id, String code, String name, WarehouseType type) {
        Warehouse warehouse = buildWarehouse(code, name, type);
        warehouse.setId(id);
        warehouse.setStatus(CommonStatus.ACTIVE);
        return warehouse;
    }

    private Location buildLocation(Long id, Long warehouseId, String zone, String rack, String row, String bin) {
        Location location = new Location();
        location.setId(id);
        location.setWarehouseId(warehouseId);
        location.setZoneCode(zone);
        location.setRackCode(rack);
        location.setRowCode(row);
        location.setBinCode(bin);
        location.setFullCode("WH01-" + zone + "-" + rack + "-" + row + "-" + bin);
        location.setLocationType(LocationType.STORAGE);
        location.setStatus(LocationStatus.ACTIVE);
        location.setUsedCapacity(0);
        return location;
    }
}
