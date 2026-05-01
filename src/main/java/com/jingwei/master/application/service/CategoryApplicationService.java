package com.jingwei.master.application.service;

import com.jingwei.master.application.dto.CreateCategoryDTO;
import com.jingwei.master.application.dto.UpdateCategoryDTO;
import com.jingwei.master.domain.model.Category;
import com.jingwei.master.domain.service.CategoryDomainService;
import com.jingwei.master.interfaces.vo.CategoryTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 物料分类应用服务
 * <p>
 * 负责分类 CRUD 的编排和事务边界管理。
 * 业务逻辑委托给 CategoryDomainService，本层只负责 DTO↔实体转换和事务控制。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryApplicationService {

    private final CategoryDomainService categoryDomainService;

    /**
     * 创建物料分类
     */
    @Transactional(rollbackFor = Exception.class)
    public CategoryTreeVO createCategory(CreateCategoryDTO dto) {
        Category category = new Category();
        category.setParentId(dto.getParentId());
        category.setCode(dto.getCode());
        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        Category saved = categoryDomainService.createCategory(category);
        return toCategoryTreeVO(saved);
    }

    /**
     * 更新物料分类
     */
    @Transactional(rollbackFor = Exception.class)
    public CategoryTreeVO updateCategory(Long categoryId, UpdateCategoryDTO dto) {
        Category updated = categoryDomainService.updateCategory(
                categoryId, dto.getCode(), dto.getName(), dto.getSortOrder(), dto.getStatus());
        return toCategoryTreeVO(updated);
    }

    /**
     * 删除物料分类
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        categoryDomainService.deleteCategory(categoryId);
    }

    /**
     * 查询分类树
     * <p>
     * 一次性加载所有分类，内存组装树形结构后返回。
     * </p>
     */
    public List<CategoryTreeVO> getCategoryTree() {
        List<Category> roots = categoryDomainService.getCategoryTree();
        return roots.stream().map(this::toCategoryTreeVO).toList();
    }

    /**
     * 根据ID查询分类详情
     */
    public CategoryTreeVO getCategoryById(Long categoryId) {
        Category category = categoryDomainService.getCategoryById(categoryId);
        return toCategoryTreeVO(category);
    }

    // ==================== 转换方法 ====================

    /**
     * 将 Category 实体（含 children）递归转换为 CategoryTreeVO
     *
     * @param category 分类实体
     * @return 分类树节点 VO
     */
    private CategoryTreeVO toCategoryTreeVO(Category category) {
        CategoryTreeVO vo = new CategoryTreeVO();
        vo.setId(category.getId());
        vo.setParentId(category.getParentId());
        vo.setCode(category.getCode());
        vo.setName(category.getName());
        vo.setLevel(category.getLevel());
        vo.setSortOrder(category.getSortOrder());
        vo.setStatus(category.getStatus().name());
        vo.setCreatedAt(category.getCreatedAt());
        vo.setUpdatedAt(category.getUpdatedAt());

        // 递归转换子节点
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            vo.setChildren(category.getChildren().stream()
                    .map(this::toCategoryTreeVO)
                    .toList());
        }

        return vo;
    }
}
