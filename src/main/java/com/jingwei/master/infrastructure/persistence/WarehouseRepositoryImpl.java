package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.master.domain.model.Warehouse;
import com.jingwei.master.domain.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 仓库仓库实现
 * <p>
 * 基于 MyBatis-Plus 实现 WarehouseRepository 接口。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class WarehouseRepositoryImpl implements WarehouseRepository {

    private final WarehouseMapper warehouseMapper;

    @Override
    public Warehouse selectById(Long id) {
        return warehouseMapper.selectById(id);
    }

    @Override
    public List<Warehouse> selectByCondition(String type, String status) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<Warehouse>()
                .orderByDesc(Warehouse::getCreatedAt);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Warehouse::getType, type);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Warehouse::getStatus, status);
        }
        return warehouseMapper.selectList(wrapper);
    }

    @Override
    public IPage<Warehouse> selectPage(IPage<Warehouse> page, String type, String status, String keyword) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<Warehouse>()
                .eq(type != null && !type.isEmpty(), Warehouse::getType, type)
                .eq(status != null && !status.isEmpty(), Warehouse::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), w ->
                        w.like(Warehouse::getCode, keyword)
                                .or()
                                .like(Warehouse::getName, keyword))
                .orderByDesc(Warehouse::getCreatedAt);
        return warehouseMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean existsByCode(String code, Long excludeId) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Warehouse::getId, excludeId);
        }
        return warehouseMapper.selectCount(wrapper) > 0;
    }

    @Override
    public long countLocationsByWarehouseId(Long warehouseId) {
        // 通过 LocationMapper 统计——此处直接用 WarehouseMapper 无法跨表，
        // 改为在 WarehouseDomainService 中注入 LocationRepository 来做引用检查
        return 0;
    }

    @Override
    public int insert(Warehouse warehouse) {
        return warehouseMapper.insert(warehouse);
    }

    @Override
    public int updateById(Warehouse warehouse) {
        return warehouseMapper.updateById(warehouse);
    }

    @Override
    public int deleteById(Long id) {
        return warehouseMapper.deleteById(id);
    }
}
