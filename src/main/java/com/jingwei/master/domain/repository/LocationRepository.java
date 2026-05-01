package com.jingwei.master.domain.repository;

import com.jingwei.master.domain.model.Location;

import java.util.List;

/**
 * 库位仓库接口
 * <p>
 * 提供库位档案的持久化操作。
 * </p>
 *
 * @author JingWei
 */
public interface LocationRepository {

    Location selectById(Long id);

    List<Location> selectByWarehouseId(Long warehouseId);

    boolean existsByWarehouseIdAndFullCode(Long warehouseId, String fullCode, Long excludeId);

    int insert(Location location);

    int updateById(Location location);

    int deleteById(Long id);

    void deleteByWarehouseId(Long warehouseId);
}
