package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.master.domain.model.Location;
import com.jingwei.master.domain.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 库位仓库实现
 * <p>
 * 基于 MyBatis-Plus 实现 LocationRepository 接口。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository {

    private final LocationMapper locationMapper;

    @Override
    public Location selectById(Long id) {
        return locationMapper.selectById(id);
    }

    @Override
    public List<Location> selectByWarehouseId(Long warehouseId) {
        return locationMapper.selectList(
                new LambdaQueryWrapper<Location>()
                        .eq(Location::getWarehouseId, warehouseId)
                        .orderByAsc(Location::getFullCode));
    }

    @Override
    public boolean existsByWarehouseIdAndFullCode(Long warehouseId, String fullCode, Long excludeId) {
        LambdaQueryWrapper<Location> wrapper = new LambdaQueryWrapper<Location>()
                .eq(Location::getWarehouseId, warehouseId)
                .eq(Location::getFullCode, fullCode);
        if (excludeId != null) {
            wrapper.ne(Location::getId, excludeId);
        }
        return locationMapper.selectCount(wrapper) > 0;
    }

    @Override
    public int insert(Location location) {
        return locationMapper.insert(location);
    }

    @Override
    public int updateById(Location location) {
        return locationMapper.updateById(location);
    }

    @Override
    public int deleteById(Long id) {
        return locationMapper.deleteById(id);
    }

    @Override
    public void deleteByWarehouseId(Long warehouseId) {
        locationMapper.delete(
                new LambdaQueryWrapper<Location>()
                        .eq(Location::getWarehouseId, warehouseId));
    }
}
