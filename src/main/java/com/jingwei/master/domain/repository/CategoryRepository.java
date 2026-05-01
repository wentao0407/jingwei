package com.jingwei.master.domain.repository;

import com.jingwei.master.domain.model.Category;

import java.util.List;

/**
 * 物料分类仓库接口
 * <p>
 * 提供物料分类的持久化操作，所有数据库操作通过此接口抽象。
 * </p>
 *
 * @author JingWei
 */
public interface CategoryRepository {

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类实体，不存在返回 null
     */
    Category selectById(Long id);

    /**
     * 查询所有分类（一次性加载，用于内存组装树）
     *
     * @return 所有未删除的分类列表，按 sort_order 排序
     */
    List<Category> selectAll();

    /**
     * 根据父级ID查询直接子分类
     *
     * @param parentId 父级分类ID，NULL 表示查询顶级分类
     * @return 子分类列表
     */
    List<Category> selectByParentId(Long parentId);

    /**
     * 插入分类
     *
     * @param category 分类实体
     * @return 影响行数
     */
    int insert(Category category);

    /**
     * 更新分类
     *
     * @param category 分类实体
     * @return 影响行数（乐观锁冲突时返回 0）
     */
    int updateById(Category category);

    /**
     * 逻辑删除分类
     *
     * @param id 分类ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 检查同级分类编码是否已存在
     * <p>
     * 同一 parent_id 下 code 不可重复。
     * </p>
     *
     * @param code     分类编码
     * @param parentId 父级分类ID
     * @param excludeId 排除的分类ID（更新时排除自身）
     * @return true=已存在
     */
    boolean existsByCodeAndParentId(String code, Long parentId, Long excludeId);

    /**
     * 检查分类是否有子分类
     *
     * @param parentId 父级分类ID
     * @return true=存在子分类
     */
    boolean hasChildren(Long parentId);

    /**
     * 统计引用该分类的物料数量
     *
     * @param categoryId 分类ID
     * @return 引用该分类的物料数量
     */
    long countMaterialsByCategoryId(Long categoryId);
}
