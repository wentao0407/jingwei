package com.jingwei.master.interfaces.controller;

import com.jingwei.common.domain.model.R;
import com.jingwei.master.application.dto.CreateCategoryDTO;
import com.jingwei.master.application.dto.UpdateCategoryDTO;
import com.jingwei.master.application.service.CategoryApplicationService;
import com.jingwei.master.interfaces.vo.CategoryTreeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 物料分类管理 Controller
 * <p>
 * 提供物料分类的 CRUD 和树查询接口。
 * 所有接口统一使用 POST 方法。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryApplicationService categoryApplicationService;

    /**
     * 创建物料分类
     */
    @PostMapping("/master/category/create")
    public R<CategoryTreeVO> createCategory(@Valid @RequestBody CreateCategoryDTO dto) {
        return R.ok(categoryApplicationService.createCategory(dto));
    }

    /**
     * 更新物料分类
     */
    @PostMapping("/master/category/update")
    public R<CategoryTreeVO> updateCategory(@RequestParam Long categoryId,
                                            @Valid @RequestBody UpdateCategoryDTO dto) {
        return R.ok(categoryApplicationService.updateCategory(categoryId, dto));
    }

    /**
     * 删除物料分类（有子分类或被物料引用时不可删除）
     */
    @PostMapping("/master/category/delete")
    public R<Void> deleteCategory(@RequestParam Long categoryId) {
        categoryApplicationService.deleteCategory(categoryId);
        return R.ok();
    }

    /**
     * 查询分类树
     * <p>
     * 返回完整的树形结构，顶级节点包含嵌套的子节点。
     * </p>
     */
    @PostMapping("/master/category/tree")
    public R<List<CategoryTreeVO>> getCategoryTree() {
        return R.ok(categoryApplicationService.getCategoryTree());
    }

    /**
     * 根据ID查询分类详情
     */
    @PostMapping("/master/category/detail")
    public R<CategoryTreeVO> getCategoryById(@RequestParam Long categoryId) {
        return R.ok(categoryApplicationService.getCategoryById(categoryId));
    }
}
