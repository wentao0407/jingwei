package com.jingwei.master.domain.repository;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.master.domain.model.Supplier;

import java.util.List;

/**
 * 供应商仓库接口
 * <p>
 * 提供供应商档案的持久化操作。
 * </p>
 *
 * @author JingWei
 */
public interface SupplierRepository {

    /**
     * 根据ID查询供应商
     *
     * @param id 供应商ID
     * @return 供应商实体，不存在返回 null
     */
    Supplier selectById(Long id);

    /**
     * 按条件查询供应商列表
     *
     * @param type                供应商类型（可选）
     * @param qualificationStatus 资质状态（可选）
     * @param status              状态（可选）
     * @return 供应商列表
     */
    List<Supplier> selectByCondition(String type, String qualificationStatus, String status);

    /**
     * 分页查询供应商
     *
     * @param page                分页参数
     * @param type                供应商类型（可选）
     * @param qualificationStatus 资质状态（可选）
     * @param status              状态（可选）
     * @param keyword             关键词（搜索编码或名称，可选）
     * @return 分页结果
     */
    IPage<Supplier> selectPage(IPage<Supplier> page, String type,
                               String qualificationStatus, String status, String keyword);

    /**
     * 检查供应商名称是否已存在
     *
     * @param name      供应商名称
     * @param excludeId 排除的ID（更新时排除自身，可为 null）
     * @return true=已存在
     */
    boolean existsByName(String name, Long excludeId);

    /**
     * 检查供应商编码是否已存在
     *
     * @param code 供应商编码
     * @return true=已存在
     */
    boolean existsByCode(String code);

    /**
     * 插入供应商
     *
     * @param supplier 供应商实体
     * @return 影响行数
     */
    int insert(Supplier supplier);

    /**
     * 更新供应商
     *
     * @param supplier 供应商实体
     * @return 影响行数（乐观锁冲突时返回 0）
     */
    int updateById(Supplier supplier);

    /**
     * 逻辑删除供应商
     *
     * @param id 供应商ID
     * @return 影响行数
     */
    int deleteById(Long id);
}
