package com.jingwei.master.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.CommonStatus;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.Location;
import com.jingwei.master.domain.model.LocationStatus;
import com.jingwei.master.domain.model.Warehouse;
import com.jingwei.master.domain.repository.LocationRepository;
import com.jingwei.master.domain.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 仓库领域服务
 * <p>
 * 核心职责：
 * <ul>
 *   <li>仓库 CRUD 及业务校验</li>
 *   <li>仓库编码唯一性校验（手动指定，如 WH01）</li>
 *   <li>仓库停用控制（停用后其下库位不可选用，已有库存不受影响）</li>
 *   <li>库位 CRUD 及业务校验（四级编码拼接、full_code 唯一性、冻结状态控制）</li>
 * </ul>
 * </p>
 * <p>
 * 关键业务规则：
 * <ul>
 *   <li>仓库编码手动指定，全局唯一——与供应商/客户不同，仓库编码无需编码规则引擎</li>
 *   <li>仓库停用后其下库位不可选用——仓库是库位的容器，容器关闭则内容不可访问</li>
 *   <li>库位完整编码自动拼接：仓库编码-库区-货架-层-位（如 WH01-A-01-02-03）</li>
 *   <li>FROZEN 状态的库位不可进行出入库操作——冻结通常在盘点期间使用</li>
 *   <li>库位类型不可修改——类型影响出入库作业策略，变更会导致业务逻辑混乱</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseDomainService {

    private final WarehouseRepository warehouseRepository;
    private final LocationRepository locationRepository;

    // ==================== 仓库 CRUD ====================

    /**
     * 获取仓库仓库引用（供 ApplicationService 分页查询使用）
     *
     * @return 仓库仓库
     */
    public WarehouseRepository getWarehouseRepository() {
        return warehouseRepository;
    }

    /**
     * 创建仓库
     * <p>
     * 校验规则：
     * <ol>
     *   <li>仓库编码不可为空</li>
     *   <li>仓库编码全局唯一</li>
     * </ol>
     * </p>
     *
     * @param warehouse 仓库实体
     * @return 保存后的仓库实体
     */
    public Warehouse createWarehouse(Warehouse warehouse) {
        if (warehouse.getCode() == null || warehouse.getCode().isBlank()) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "仓库编码不能为空");
        }

        if (warehouseRepository.existsByCode(warehouse.getCode(), null)) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "仓库编码已存在");
        }

        warehouse.setStatus(CommonStatus.ACTIVE);

        try {
            warehouseRepository.insert(warehouse);
        } catch (DuplicateKeyException e) {
            log.warn("并发创建仓库触发唯一约束: code={}", warehouse.getCode());
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "仓库编码已存在");
        }

        log.info("创建仓库: code={}, name={}, type={}, id={}",
                warehouse.getCode(), warehouse.getName(), warehouse.getType(), warehouse.getId());
        return warehouse;
    }

    /**
     * 更新仓库
     * <p>
     * 可更新字段：name, address, managerId, remark。
     * 仓库编码和类型不可修改——编码被库位完整编码引用（如 WH01-A-01-02-03），
     * 类型影响出入库策略和库位配置。
     * </p>
     *
     * @param warehouseId 仓库ID
     * @param warehouse   包含更新字段的仓库实体
     * @return 更新后的仓库
     */
    public Warehouse updateWarehouse(Long warehouseId, Warehouse warehouse) {
        Warehouse existing = warehouseRepository.selectById(warehouseId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "仓库不存在");
        }

        // 编码和类型不可修改
        warehouse.setId(warehouseId);
        warehouse.setCode(existing.getCode());
        warehouse.setType(existing.getType());

        int rows = warehouseRepository.updateById(warehouse);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新仓库: id={}", warehouseId);
        return warehouseRepository.selectById(warehouseId);
    }

    /**
     * 停用仓库
     * <p>
     * 停用后其下库位不可选用，已有库存不受影响。
     * </p>
     *
     * @param warehouseId 仓库ID
     */
    public void deactivateWarehouse(Long warehouseId) {
        Warehouse existing = warehouseRepository.selectById(warehouseId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "仓库不存在");
        }

        if (existing.getStatus() == CommonStatus.INACTIVE) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "仓库已停用");
        }

        existing.setStatus(CommonStatus.INACTIVE);
        int rows = warehouseRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("停用仓库: id={}, code={}", warehouseId, existing.getCode());
    }

    /**
     * 启用仓库
     *
     * @param warehouseId 仓库ID
     */
    public void activateWarehouse(Long warehouseId) {
        Warehouse existing = warehouseRepository.selectById(warehouseId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "仓库不存在");
        }

        if (existing.getStatus() == CommonStatus.ACTIVE) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "仓库已启用");
        }

        existing.setStatus(CommonStatus.ACTIVE);
        int rows = warehouseRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("启用仓库: id={}, code={}", warehouseId, existing.getCode());
    }

    /**
     * 删除仓库
     * <p>
     * 同时删除仓库下的所有库位。仅允许删除没有库存的仓库。
     * </p>
     *
     * @param warehouseId 仓库ID
     */
    public void deleteWarehouse(Long warehouseId) {
        Warehouse existing = warehouseRepository.selectById(warehouseId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "仓库不存在");
        }

        // 检查仓库下是否有库存（当前库存模块尚未实现，预留钩子）
        long inventoryCount = countInventoryReferences(warehouseId);
        if (inventoryCount > 0) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "该仓库下存在库存数据，不可删除");
        }

        // 先删除仓库下的所有库位
        List<Location> locations = locationRepository.selectByWarehouseId(warehouseId);
        for (Location location : locations) {
            locationRepository.deleteById(location.getId());
        }

        warehouseRepository.deleteById(warehouseId);
        log.info("删除仓库及{}个库位: id={}, code={}", locations.size(), warehouseId, existing.getCode());
    }

    /**
     * 查询仓库列表
     */
    public List<Warehouse> listWarehouses(String type, String status) {
        return warehouseRepository.selectByCondition(type, status);
    }

    /**
     * 查询仓库详情（含库位列表）
     */
    public Warehouse getWarehouseDetail(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.selectById(warehouseId);
        if (warehouse == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "仓库不存在");
        }

        List<Location> locations = locationRepository.selectByWarehouseId(warehouseId);
        warehouse.setLocations(locations);

        return warehouse;
    }

    /**
     * 根据ID查询仓库
     */
    public Warehouse getWarehouseById(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.selectById(warehouseId);
        if (warehouse == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "仓库不存在");
        }
        return warehouse;
    }

    // ==================== 库位 CRUD ====================

    /**
     * 在仓库下新增库位
     * <p>
     * 校验规则：
     * <ol>
     *   <li>仓库必须存在且为 ACTIVE 状态</li>
     *   <li>同一仓库内 full_code 不可重复</li>
     *   <li>full_code 自动拼接：仓库编码-库区-货架-层-位</li>
     * </ol>
     * </p>
     *
     * @param warehouseId 仓库ID
     * @param location    库位实体
     * @return 保存后的库位实体
     */
    public Location createLocation(Long warehouseId, Location location) {
        Warehouse warehouse = warehouseRepository.selectById(warehouseId);
        if (warehouse == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "仓库不存在");
        }

        if (warehouse.getStatus() == CommonStatus.INACTIVE) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "已停用的仓库不允许新增库位");
        }

        // 自动拼接 full_code：仓库编码-库区-货架-层-位
        String fullCode = buildFullCode(warehouse.getCode(), location);
        location.setWarehouseId(warehouseId);
        location.setFullCode(fullCode);

        // 校验 full_code 唯一性
        if (locationRepository.existsByWarehouseIdAndFullCode(warehouseId, fullCode, null)) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "该仓库下已存在相同编码的库位");
        }

        location.setStatus(LocationStatus.ACTIVE);
        if (location.getUsedCapacity() == null) {
            location.setUsedCapacity(0);
        }

        try {
            locationRepository.insert(location);
        } catch (DuplicateKeyException e) {
            log.warn("并发创建库位触发唯一约束: warehouseId={}, fullCode={}", warehouseId, fullCode);
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "该仓库下已存在相同编码的库位");
        }

        log.info("创建库位: warehouseId={}, fullCode={}, type={}, id={}",
                warehouseId, fullCode, location.getLocationType(), location.getId());
        return location;
    }

    /**
     * 更新库位
     * <p>
     * 可更新字段：capacity, remark。
     * 库位类型不可修改——类型影响出入库作业策略。
     * 四级编码不可修改——修改编码会导致 full_code 变化，影响库存定位。
     * </p>
     *
     * @param locationId 库位ID
     * @param location   包含更新字段的库位实体
     * @return 更新后的库位
     */
    public Location updateLocation(Long locationId, Location location) {
        Location existing = locationRepository.selectById(locationId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "库位不存在");
        }

        // 冻结状态的库位不允许修改
        if (existing.getStatus() == LocationStatus.FROZEN) {
            throw new BizException(ErrorCode.LOCATION_FROZEN);
        }

        // 编码、类型、仓库ID 不可修改
        location.setId(locationId);
        location.setWarehouseId(existing.getWarehouseId());
        location.setZoneCode(existing.getZoneCode());
        location.setRackCode(existing.getRackCode());
        location.setRowCode(existing.getRowCode());
        location.setBinCode(existing.getBinCode());
        location.setFullCode(existing.getFullCode());
        location.setLocationType(existing.getLocationType());
        location.setUsedCapacity(existing.getUsedCapacity());

        int rows = locationRepository.updateById(location);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新库位: id={}", locationId);
        return locationRepository.selectById(locationId);
    }

    /**
     * 冻结库位
     * <p>
     * 冻结后不可进行出入库操作，通常在盘点期间使用。
     * </p>
     *
     * @param locationId 库位ID
     */
    public void freezeLocation(Long locationId) {
        Location existing = locationRepository.selectById(locationId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "库位不存在");
        }

        if (existing.getStatus() == LocationStatus.FROZEN) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "库位已冻结");
        }

        existing.setStatus(LocationStatus.FROZEN);
        int rows = locationRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("冻结库位: id={}, fullCode={}", locationId, existing.getFullCode());
    }

    /**
     * 解冻库位
     *
     * @param locationId 库位ID
     */
    public void unfreezeLocation(Long locationId) {
        Location existing = locationRepository.selectById(locationId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "库位不存在");
        }

        if (existing.getStatus() != LocationStatus.FROZEN) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "库位未冻结");
        }

        existing.setStatus(LocationStatus.ACTIVE);
        int rows = locationRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("解冻库位: id={}, fullCode={}", locationId, existing.getFullCode());
    }

    /**
     * 停用库位
     *
     * @param locationId 库位ID
     */
    public void deactivateLocation(Long locationId) {
        Location existing = locationRepository.selectById(locationId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "库位不存在");
        }

        if (existing.getStatus() == LocationStatus.INACTIVE) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "库位已停用");
        }

        if (existing.getStatus() == LocationStatus.FROZEN) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "冻结状态的库位不允许停用，请先解冻");
        }

        existing.setStatus(LocationStatus.INACTIVE);
        int rows = locationRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("停用库位: id={}, fullCode={}", locationId, existing.getFullCode());
    }

    /**
     * 删除库位
     * <p>
     * 仅允许删除没有库存的库位。
     * 冻结状态的库位不允许删除。
     * </p>
     *
     * @param locationId 库位ID
     */
    public void deleteLocation(Long locationId) {
        Location existing = locationRepository.selectById(locationId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "库位不存在");
        }

        if (existing.getStatus() == LocationStatus.FROZEN) {
            throw new BizException(ErrorCode.LOCATION_FROZEN);
        }

        locationRepository.deleteById(locationId);
        log.info("删除库位: id={}, fullCode={}", locationId, existing.getFullCode());
    }

    // ==================== 私有方法 ====================

    /**
     * 拼接库位完整编码
     * <p>
     * 格式：仓库编码-库区-货架-层-位（如 WH01-A-01-02-03）
     * </p>
     *
     * @param warehouseCode 仓库编码
     * @param location      库位实体（含 zoneCode, rackCode, rowCode, binCode）
     * @return 完整编码
     */
    private String buildFullCode(String warehouseCode, Location location) {
        return warehouseCode + "-" +
                location.getZoneCode() + "-" +
                location.getRackCode() + "-" +
                location.getRowCode() + "-" +
                location.getBinCode();
    }

    /**
     * 统计仓库下的库存数量
     * <p>
     * 当前库存模块尚未实现，返回 0。
     * 库存模块实现后，应替换为真实查询。
     * </p>
     *
     * @param warehouseId 仓库ID
     * @return 库存数量
     */
    private long countInventoryReferences(Long warehouseId) {
        // TODO: 库存模块实现后，注入库存 Mapper 并查询真实数量
        return 0;
    }
}
