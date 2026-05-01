package com.jingwei.master.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.master.domain.model.Category;
import com.jingwei.master.domain.model.Material;
import com.jingwei.master.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 物料分类仓库实现
 * <p>
 * 基于 MyBatis-Plus 实现 CategoryRepository 接口。
 * 查询操作使用 LambdaQueryWrapper 构建条件，保证类型安全。
 * </p>
 * <p>
 * {@link #countMaterialsByCategoryId(Long)} 使用 MaterialMapper 查询物料引用数量，
 * 为分类删除保护提供真实的数据支撑。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryMapper categoryMapper;
    private final MaterialMapper materialMapper;

    @Override
    public Category selectById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public List<Category> selectAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSortOrder)
                        .orderByAsc(Category::getCode));
    }

    @Override
    public List<Category> selectByParentId(Long parentId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder);
        if (parentId == null) {
            // 查询顶级分类（parent_id IS NULL）
            wrapper.isNull(Category::getParentId);
        } else {
            wrapper.eq(Category::getParentId, parentId);
        }
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public int insert(Category category) {
        return categoryMapper.insert(category);
    }

    @Override
    public int updateById(Category category) {
        return categoryMapper.updateById(category);
    }

    @Override
    public int deleteById(Long id) {
        return categoryMapper.deleteById(id);
    }

    @Override
    public boolean existsByCodeAndParentId(String code, Long parentId, Long excludeId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .eq(Category::getCode, code);

        // parentId 为 null 时查询顶级分类，否则查询指定父级下的分类
        if (parentId == null) {
            wrapper.isNull(Category::getParentId);
        } else {
            wrapper.eq(Category::getParentId, parentId);
        }

        // 排除自身（更新场景）
        if (excludeId != null) {
            wrapper.ne(Category::getId, excludeId);
        }

        return categoryMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean hasChildren(Long parentId) {
        return categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getParentId, parentId)) > 0;
    }

    /**
     * 统计引用该分类的物料数量
     * <p>
     * 使用 MaterialMapper 查询 t_md_material 表中 category_id 匹配且未删除的记录数。
     * 分类删除保护的核心数据来源：被物料引用的分类不可删除。
     * </p>
     *
     * @param categoryId 分类ID
     * @return 引用该分类的物料数量
     */
    @Override
    public long countMaterialsByCategoryId(Long categoryId) {
        return materialMapper.selectCount(
                new LambdaQueryWrapper<Material>()
                        .eq(Material::getCategoryId, categoryId));
    }
}
