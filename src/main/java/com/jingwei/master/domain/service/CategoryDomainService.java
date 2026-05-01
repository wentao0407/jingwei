package com.jingwei.master.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.CommonStatus;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.Category;
import com.jingwei.master.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 物料分类领域服务
 * <p>
 * 核心职责：
 * <ul>
 *   <li>分类 CRUD 及业务校验（层级限制、同级编码唯一、引用检查）</li>
 *   <li>分类树查询（一次性加载所有记录，内存中组装树形结构）</li>
 * </ul>
 * </p>
 * <p>
 * 设计决策：
 * <ul>
 *   <li>树形结构使用 parent_id 字段，不用嵌套集——简单直观，3级深度不需要嵌套集的查询优势</li>
 *   <li>查询树时一次性查所有记录，在内存中组装——数据量小（通常几十条），避免 N+1 查询</li>
 *   <li>层级最多3级，超过抛出 BizException——服装行业分类不需要更深的层级</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryDomainService {

    /** 分类最大层级 */
    private static final int MAX_LEVEL = 3;

    private final CategoryRepository categoryRepository;

    // ==================== 分类 CRUD ====================

    /**
     * 创建物料分类
     * <p>
     * 校验规则：
     * <ol>
     *   <li>同级分类编码不可重复</li>
     *   <li>层级不能超过3级（父分类为2级时，子分类为3级，不允许再创建4级）</li>
     *   <li>父分类必须存在且状态为 ACTIVE</li>
     * </ol>
     * </p>
     *
     * @param category 分类实体（不含 level，level 自动计算）
     * @return 保存后的分类实体
     */
    public Category createCategory(Category category) {
        // 计算 level：有 parentId 则查父分类确定 level，否则为1级
        if (category.getParentId() != null) {
            Category parent = categoryRepository.selectById(category.getParentId());
            if (parent == null) {
                throw new BizException(ErrorCode.DATA_NOT_FOUND, "父级分类不存在");
            }
            if (parent.getStatus() == CommonStatus.INACTIVE) {
                throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "父级分类已停用，不可在其下创建子分类");
            }
            int childLevel = parent.getLevel() + 1;
            if (childLevel > MAX_LEVEL) {
                throw new BizException(ErrorCode.CATEGORY_LEVEL_EXCEEDED);
            }
            category.setLevel(childLevel);
        } else {
            category.setLevel(1);
        }

        // 校验同级编码唯一性（应用层提前拦截，避免大多数情况下触发数据库约束）
        if (categoryRepository.existsByCodeAndParentId(category.getCode(), category.getParentId(), null)) {
            throw new BizException(ErrorCode.CATEGORY_CODE_DUPLICATE);
        }

        category.setStatus(CommonStatus.ACTIVE);
        try {
            categoryRepository.insert(category);
        } catch (DuplicateKeyException e) {
            // 并发场景下，两个请求同时通过应用层校验后竞争 INSERT，
            // 数据库唯一索引（COALESCE(parent_id, 0)）兜底拦截第二个请求
            log.warn("并发创建分类触发唯一约束: code={}, parentId={}", category.getCode(), category.getParentId());
            throw new BizException(ErrorCode.CATEGORY_CODE_DUPLICATE);
        }

        log.info("创建物料分类: code={}, name={}, level={}, id={}",
                category.getCode(), category.getName(), category.getLevel(), category.getId());
        return category;
    }

    /**
     * 更新物料分类
     * <p>
     * 可更新字段：code, name, sortOrder, status。
     * 不允许变更 parentId 和 level（如需移动分类，应删除后重建）。
     * </p>
     *
     * @param categoryId 分类ID
     * @param code       新编码（可为 null，不更新）
     * @param name       新名称（可为 null，不更新）
     * @param sortOrder  新排序号（可为 null，不更新）
     * @param status     新状态（可为 null，不更新）
     * @return 更新后的分类
     */
    public Category updateCategory(Long categoryId, String code, String name,
                                   Integer sortOrder, String status) {
        Category existing = categoryRepository.selectById(categoryId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "物料分类不存在");
        }

        // 如果修改了编码，校验同级唯一性
        if (code != null && !code.equals(existing.getCode())) {
            if (categoryRepository.existsByCodeAndParentId(code, existing.getParentId(), categoryId)) {
                throw new BizException(ErrorCode.CATEGORY_CODE_DUPLICATE);
            }
            existing.setCode(code);
        }

        if (name != null) {
            existing.setName(name);
        }
        if (sortOrder != null) {
            existing.setSortOrder(sortOrder);
        }
        if (status != null) {
            existing.setStatus(CommonStatus.valueOf(status));
        }

        int rows = categoryRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新物料分类: id={}", categoryId);
        return categoryRepository.selectById(categoryId);
    }

    /**
     * 删除物料分类
     * <p>
     * 校验规则：
     * <ol>
     *   <li>存在子分类时不可删除——先删除或移动子分类</li>
     *   <li>已被物料引用时不可删除——提示被引用数量</li>
     * </ol>
     * </p>
     *
     * @param categoryId 分类ID
     */
    public void deleteCategory(Long categoryId) {
        Category existing = categoryRepository.selectById(categoryId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "物料分类不存在");
        }

        // 检查是否有子分类
        if (categoryRepository.hasChildren(categoryId)) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "该分类存在子分类，不可删除");
        }

        // 检查是否被物料引用
        long materialCount = categoryRepository.countMaterialsByCategoryId(categoryId);
        if (materialCount > 0) {
            throw new BizException(ErrorCode.CATEGORY_REFERENCED_BY_MATERIAL,
                    "该分类已被" + materialCount + "个物料引用，不可删除");
        }

        categoryRepository.deleteById(categoryId);
        log.info("删除物料分类: id={}, code={}, name={}", categoryId, existing.getCode(), existing.getName());
    }

    // ==================== 分类查询 ====================

    /**
     * 查询分类树
     * <p>
     * 策略：一次性查询所有未删除的分类记录，在内存中组装树形结构。
     * 数据量通常在几十条，无需分页，也避免了递归查询数据库。
     * </p>
     *
     * @return 顶级分类列表（每个节点包含 children 子列表）
     */
    public List<Category> getCategoryTree() {
        List<Category> allCategories = categoryRepository.selectAll();
        return buildTree(allCategories);
    }

    /**
     * 根据ID查询分类详情
     *
     * @param categoryId 分类ID
     * @return 分类实体
     */
    public Category getCategoryById(Long categoryId) {
        Category category = categoryRepository.selectById(categoryId);
        if (category == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "物料分类不存在");
        }
        return category;
    }

    // ==================== 私有方法 ====================

    /**
     * 将扁平列表组装为树形结构
     * <p>
     * 算法：
     * <ol>
     *   <li>将所有分类按 ID 建立索引 Map</li>
     *   <li>遍历所有分类，将每个分类挂到其 parentId 对应的 children 列表下</li>
     *   <li>parentId 为 null 的即为顶级节点</li>
     * </ol>
     * 时间复杂度 O(n)，空间复杂度 O(n)。
     * </p>
     *
     * @param allCategories 所有分类的扁平列表
     * @return 顶级分类列表（含 children）
     */
    private List<Category> buildTree(List<Category> allCategories) {
        // 按 ID 建立索引，用于快速查找父节点
        Map<Long, Category> categoryMap = new LinkedHashMap<>();
        for (Category cat : allCategories) {
            categoryMap.put(cat.getId(), cat);
        }

        List<Category> roots = new ArrayList<>();
        for (Category cat : allCategories) {
            if (cat.getParentId() == null) {
                // 顶级节点
                roots.add(cat);
            } else {
                // 挂到父节点的 children 下
                Category parent = categoryMap.get(cat.getParentId());
                if (parent != null) {
                    parent.addChild(cat);
                }
                // 如果父节点不在结果集中（已被删除等异常情况），作为孤立节点忽略
            }
        }

        return roots;
    }
}
