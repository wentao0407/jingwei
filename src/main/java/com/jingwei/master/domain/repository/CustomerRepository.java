package com.jingwei.master.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.master.domain.model.Customer;

import java.util.List;

/**
 * 客户仓库接口
 * <p>
 * 提供客户档案的持久化操作。
 * </p>
 *
 * @author JingWei
 */
public interface CustomerRepository {

    /**
     * 根据ID查询客户
     *
     * @param id 客户ID
     * @return 客户实体，不存在返回 null
     */
    Customer selectById(Long id);

    /**
     * 按条件查询客户列表
     *
     * @param type   客户类型（可选）
     * @param level  客户等级（可选）
     * @param status 状态（可选）
     * @return 客户列表
     */
    List<Customer> selectByCondition(String type, String level, String status);

    /**
     * 分页查询客户
     *
     * @param page    分页参数
     * @param type    客户类型（可选）
     * @param level   客户等级（可选）
     * @param status  状态（可选）
     * @param keyword 关键词（搜索编码或名称，可选）
     * @return 分页结果
     */
    IPage<Customer> selectPage(IPage<Customer> page, String type,
                               String level, String status, String keyword);

    /**
     * 检查客户名称是否已存在
     *
     * @param name      客户名称
     * @param excludeId 排除的ID（更新时排除自身，可为 null）
     * @return true=已存在
     */
    boolean existsByName(String name, Long excludeId);

    /**
     * 检查客户编码是否已存在
     *
     * @param code 客户编码
     * @return true=已存在
     */
    boolean existsByCode(String code);

    /**
     * 插入客户
     *
     * @param customer 客户实体
     * @return 影响行数
     */
    int insert(Customer customer);

    /**
     * 更新客户
     *
     * @param customer 客户实体
     * @return 影响行数（乐观锁冲突时返回 0）
     */
    int updateById(Customer customer);

    /**
     * 逻辑删除客户
     *
     * @param id 客户ID
     * @return 影响行数
     */
    int deleteById(Long id);
}
