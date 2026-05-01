package com.jingwei.master.domain.repository;

import com.jingwei.master.domain.model.Supplier;

import java.util.List;

/**
 * 供应商仓库接口
 *
 * @author JingWei
 */
public interface SupplierRepository {

    Supplier selectById(Long id);

    List<Supplier> selectByCondition(String type, String qualificationStatus, String status);

    boolean existsByName(String name, Long excludeId);

    int insert(Supplier supplier);

    int updateById(Supplier supplier);
}
