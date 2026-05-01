package com.jingwei.master.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.master.domain.model.Warehouse;

import java.util.List;

/**
 * 仓库仓库接口
 * <p>
 * 提供仓库档案的持久化操作。
 * </p>
 *
 * @author JingWei
 */
public interface WarehouseRepository {

    Warehouse selectById(Long id);

    List<Warehouse> selectByCondition(String type, String status);

    IPage<Warehouse> selectPage(IPage<Warehouse> page, String type, String status, String keyword);

    boolean existsByCode(String code, Long excludeId);

    long countLocationsByWarehouseId(Long warehouseId);

    int insert(Warehouse warehouse);

    int updateById(Warehouse warehouse);

    int deleteById(Long id);
}
