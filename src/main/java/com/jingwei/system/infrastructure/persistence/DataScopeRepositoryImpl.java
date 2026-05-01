package com.jingwei.system.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.system.domain.model.DataScope;
import com.jingwei.system.domain.repository.DataScopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DataScopeRepositoryImpl implements DataScopeRepository {

    private final DataScopeMapper dataScopeMapper;

    @Override
    public List<DataScope> selectByRoleId(Long roleId) {
        return dataScopeMapper.selectList(
                new LambdaQueryWrapper<DataScope>()
                        .eq(DataScope::getRoleId, roleId));
    }

    @Override
    public boolean existsByRoleIdAndScopeType(Long roleId, String scopeType) {
        return dataScopeMapper.selectCount(
                new LambdaQueryWrapper<DataScope>()
                        .eq(DataScope::getRoleId, roleId)
                        .eq(DataScope::getScopeType, scopeType)) > 0;
    }

    @Override
    public int insert(DataScope dataScope) {
        return dataScopeMapper.insert(dataScope);
    }

    @Override
    public int updateByRoleIdAndScopeType(DataScope dataScope) {
        LambdaQueryWrapper<DataScope> wrapper = new LambdaQueryWrapper<DataScope>()
                .eq(DataScope::getRoleId, dataScope.getRoleId())
                .eq(DataScope::getScopeType, dataScope.getScopeType());
        return dataScopeMapper.update(dataScope, wrapper);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        dataScopeMapper.delete(
                new LambdaQueryWrapper<DataScope>()
                        .eq(DataScope::getRoleId, roleId));
    }
}
