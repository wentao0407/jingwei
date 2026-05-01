package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.master.domain.model.CommonStatus;
import com.jingwei.master.domain.model.Supplier;
import com.jingwei.master.domain.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 供应商仓库实现
 * <p>
 * 基于 MyBatis-Plus 实现 SupplierRepository 接口。
 * 查询操作使用 LambdaQueryWrapper 构建条件，保证类型安全。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SupplierRepositoryImpl implements SupplierRepository {

    private final SupplierMapper supplierMapper;

    @Override
    public Supplier selectById(Long id) {
        return supplierMapper.selectById(id);
    }

    @Override
    public List<Supplier> selectByCondition(String type, String qualificationStatus, String status) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<Supplier>()
                .orderByDesc(Supplier::getCreatedAt);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Supplier::getType, type);
        }
        if (qualificationStatus != null && !qualificationStatus.isEmpty()) {
            wrapper.eq(Supplier::getQualificationStatus, qualificationStatus);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Supplier::getStatus, status);
        }
        return supplierMapper.selectList(wrapper);
    }

    @Override
    public boolean existsByName(String name, Long excludeId) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getName, name);
        if (excludeId != null) {
            wrapper.ne(Supplier::getId, excludeId);
        }
        return supplierMapper.selectCount(wrapper) > 0;
    }

    @Override
    public int insert(Supplier supplier) {
        return supplierMapper.insert(supplier);
    }

    @Override
    public int updateById(Supplier supplier) {
        return supplierMapper.updateById(supplier);
    }
}
