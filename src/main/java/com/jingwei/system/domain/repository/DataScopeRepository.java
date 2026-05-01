package com.jingwei.system.domain.repository;

import com.jingwei.system.domain.model.DataScope;

import java.util.List;

/**
 * 数据权限仓库接口
 *
 * @author JingWei
 */
public interface DataScopeRepository {

    List<DataScope> selectByRoleId(Long roleId);

    boolean existsByRoleIdAndScopeType(Long roleId, String scopeType);

    int insert(DataScope dataScope);

    int updateByRoleIdAndScopeType(DataScope dataScope);

    void deleteByRoleId(Long roleId);
}
