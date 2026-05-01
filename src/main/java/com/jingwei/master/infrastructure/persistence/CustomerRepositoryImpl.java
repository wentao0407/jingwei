package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.master.domain.model.Customer;
import com.jingwei.master.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 客户仓库实现
 * <p>
 * 基于 MyBatis-Plus 实现 CustomerRepository 接口。
 * 查询操作使用 LambdaQueryWrapper 构建条件，保证类型安全。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerMapper customerMapper;

    @Override
    public Customer selectById(Long id) {
        return customerMapper.selectById(id);
    }

    @Override
    public List<Customer> selectByCondition(String type, String level, String status) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<Customer>()
                .orderByDesc(Customer::getCreatedAt);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Customer::getType, type);
        }
        if (level != null && !level.isEmpty()) {
            wrapper.eq(Customer::getLevel, level);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Customer::getStatus, status);
        }
        return customerMapper.selectList(wrapper);
    }

    @Override
    public IPage<Customer> selectPage(IPage<Customer> page, String type,
                                      String level, String status, String keyword) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<Customer>()
                .eq(type != null && !type.isEmpty(), Customer::getType, type)
                .eq(level != null && !level.isEmpty(), Customer::getLevel, level)
                .eq(status != null && !status.isEmpty(), Customer::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), w ->
                        w.like(Customer::getCode, keyword)
                                .or()
                                .like(Customer::getName, keyword))
                .orderByDesc(Customer::getCreatedAt);
        return customerMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean existsByName(String name, Long excludeId) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<Customer>()
                .eq(Customer::getName, name);
        if (excludeId != null) {
            wrapper.ne(Customer::getId, excludeId);
        }
        return customerMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByCode(String code) {
        return customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getCode, code)) > 0;
    }

    @Override
    public int insert(Customer customer) {
        return customerMapper.insert(customer);
    }

    @Override
    public int updateById(Customer customer) {
        return customerMapper.updateById(customer);
    }

    @Override
    public int deleteById(Long id) {
        return customerMapper.deleteById(id);
    }
}
