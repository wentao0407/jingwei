package com.jingwei.master;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.CommonStatus;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.Category;
import com.jingwei.master.domain.repository.CategoryRepository;
import com.jingwei.master.domain.service.CategoryDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CategoryDomainService 单元测试
 * <p>
 * 测试物料分类领域服务的核心业务规则：
 * <ul>
 *   <li>创建分类：层级自动计算、层级超限校验、同级编码唯一性校验</li>
 *   <li>更新分类：编码修改时的唯一性校验</li>
 *   <li>删除分类：子分类检查、物料引用检查</li>
 *   <li>分类树查询：扁平列表组装为树形结构</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class CategoryDomainServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryDomainService categoryDomainService;

    // ==================== 创建分类 ====================

    @Test
    @DisplayName("创建顶级分类 — level 应为1")
    void createCategory_topLevel_levelShouldBe1() {
        Category category = new Category();
        category.setCode("FABRIC");
        category.setName("面料");
        category.setParentId(null);

        when(categoryRepository.existsByCodeAndParentId("FABRIC", null, null)).thenReturn(false);
        when(categoryRepository.insert(any())).thenReturn(1);

        Category result = categoryDomainService.createCategory(category);

        assertEquals(1, result.getLevel());
        assertEquals(CommonStatus.ACTIVE, result.getStatus());
    }

    @Test
    @DisplayName("创建2级分类 — level 应为2")
    void createCategory_secondLevel_levelShouldBe2() {
        Category parent = buildCategory(1L, null, "PRODUCT", "成品", 1);
        when(categoryRepository.selectById(1L)).thenReturn(parent);
        when(categoryRepository.existsByCodeAndParentId("WOMEN", 1L, null)).thenReturn(false);
        when(categoryRepository.insert(any())).thenReturn(1);

        Category category = new Category();
        category.setCode("WOMEN");
        category.setName("女装");
        category.setParentId(1L);

        Category result = categoryDomainService.createCategory(category);

        assertEquals(2, result.getLevel());
    }

    @Test
    @DisplayName("创建3级分类 — level 应为3")
    void createCategory_thirdLevel_levelShouldBe3() {
        Category parent = buildCategory(2L, 1L, "WOMEN", "女装", 2);
        when(categoryRepository.selectById(2L)).thenReturn(parent);
        when(categoryRepository.existsByCodeAndParentId("COAT", 2L, null)).thenReturn(false);
        when(categoryRepository.insert(any())).thenReturn(1);

        Category category = new Category();
        category.setCode("COAT");
        category.setName("外套");
        category.setParentId(2L);

        Category result = categoryDomainService.createCategory(category);

        assertEquals(3, result.getLevel());
    }

    @Test
    @DisplayName("创建4级分类 — 应抛异常 CATEGORY_LEVEL_EXCEEDED")
    void createCategory_fourthLevel_shouldThrow() {
        Category parent = buildCategory(3L, 2L, "COAT", "外套", 3);
        when(categoryRepository.selectById(3L)).thenReturn(parent);

        Category category = new Category();
        category.setCode("DOWN_COAT");
        category.setName("羽绒服");
        category.setParentId(3L);

        BizException ex = assertThrows(BizException.class,
                () -> categoryDomainService.createCategory(category));
        assertEquals(ErrorCode.CATEGORY_LEVEL_EXCEEDED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("创建分类 — 同级编码重复应抛异常")
    void createCategory_duplicateCode_shouldThrow() {
        when(categoryRepository.existsByCodeAndParentId("FABRIC", null, null)).thenReturn(true);

        Category category = new Category();
        category.setCode("FABRIC");
        category.setName("面料");
        category.setParentId(null);

        BizException ex = assertThrows(BizException.class,
                () -> categoryDomainService.createCategory(category));
        assertEquals(ErrorCode.CATEGORY_CODE_DUPLICATE.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("创建分类 — 父分类不存在应抛异常")
    void createCategory_parentNotFound_shouldThrow() {
        when(categoryRepository.selectById(999L)).thenReturn(null);

        Category category = new Category();
        category.setCode("NEW");
        category.setName("新分类");
        category.setParentId(999L);

        BizException ex = assertThrows(BizException.class,
                () -> categoryDomainService.createCategory(category));
        assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("父级分类不存在"));
    }

    @Test
    @DisplayName("创建分类 — 父分类已停用应抛异常")
    void createCategory_parentInactive_shouldThrow() {
        Category parent = buildCategory(1L, null, "PRODUCT", "成品", 1);
        parent.setStatus(CommonStatus.INACTIVE);
        when(categoryRepository.selectById(1L)).thenReturn(parent);

        Category category = new Category();
        category.setCode("WOMEN");
        category.setName("女装");
        category.setParentId(1L);

        BizException ex = assertThrows(BizException.class,
                () -> categoryDomainService.createCategory(category));
        assertTrue(ex.getMessage().contains("停用"));
    }

    // ==================== 更新分类 ====================

    @Test
    @DisplayName("更新分类 — 修改编码时同级唯一性校验")
    void updateCategory_changeCode_duplicate_shouldThrow() {
        Category existing = buildCategory(1L, null, "FABRIC", "面料", 1);
        when(categoryRepository.selectById(1L)).thenReturn(existing);
        when(categoryRepository.existsByCodeAndParentId("NEW_CODE", null, 1L)).thenReturn(true);

        BizException ex = assertThrows(BizException.class,
                () -> categoryDomainService.updateCategory(1L, "NEW_CODE", null, null, null));
        assertEquals(ErrorCode.CATEGORY_CODE_DUPLICATE.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("更新分类 — 正常更新应成功")
    void updateCategory_shouldSucceed() {
        Category existing = buildCategory(1L, null, "FABRIC", "面料", 1);
        when(categoryRepository.selectById(1L)).thenReturn(existing);
        when(categoryRepository.updateById(any())).thenReturn(1);

        Category updated = buildCategory(1L, null, "FABRIC", "面料（更新）", 1);
        updated.setName("面料（更新）");
        when(categoryRepository.selectById(1L)).thenReturn(existing);

        assertDoesNotThrow(() ->
                categoryDomainService.updateCategory(1L, null, "面料（更新）", null, null));
    }

    @Test
    @DisplayName("更新分类 — 分类不存在应抛异常")
    void updateCategory_notFound_shouldThrow() {
        when(categoryRepository.selectById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> categoryDomainService.updateCategory(999L, "CODE", null, null, null));
        assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
    }

    // ==================== 删除分类 ====================

    @Test
    @DisplayName("删除分类 — 存在子分类应抛异常")
    void deleteCategory_hasChildren_shouldThrow() {
        Category existing = buildCategory(1L, null, "PRODUCT", "成品", 1);
        when(categoryRepository.selectById(1L)).thenReturn(existing);
        when(categoryRepository.hasChildren(1L)).thenReturn(true);

        BizException ex = assertThrows(BizException.class,
                () -> categoryDomainService.deleteCategory(1L));
        assertTrue(ex.getMessage().contains("子分类"));
    }

    @Test
    @DisplayName("删除分类 — 被物料引用应抛异常")
    void deleteCategory_referencedByMaterial_shouldThrow() {
        Category existing = buildCategory(1L, null, "FABRIC", "面料", 1);
        when(categoryRepository.selectById(1L)).thenReturn(existing);
        when(categoryRepository.hasChildren(1L)).thenReturn(false);
        when(categoryRepository.countMaterialsByCategoryId(1L)).thenReturn(5L);

        BizException ex = assertThrows(BizException.class,
                () -> categoryDomainService.deleteCategory(1L));
        assertEquals(ErrorCode.CATEGORY_REFERENCED_BY_MATERIAL.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("5"));
    }

    @Test
    @DisplayName("删除分类 — 无子分类且无引用应成功")
    void deleteCategory_noChildrenNoReference_shouldSucceed() {
        Category existing = buildCategory(1L, null, "FABRIC", "面料", 1);
        when(categoryRepository.selectById(1L)).thenReturn(existing);
        when(categoryRepository.hasChildren(1L)).thenReturn(false);
        when(categoryRepository.countMaterialsByCategoryId(1L)).thenReturn(0L);
        when(categoryRepository.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> categoryDomainService.deleteCategory(1L));
        verify(categoryRepository).deleteById(1L);
    }

    // ==================== 分类树查询 ====================

    @Test
    @DisplayName("分类树 — 正确组装三层树形结构")
    void getCategoryTree_shouldBuildCorrectTree() {
        Category product = buildCategory(1L, null, "PRODUCT", "成品", 1);
        Category fabric = buildCategory(2L, null, "FABRIC", "面料", 1);
        Category women = buildCategory(3L, 1L, "WOMEN", "女装", 2);
        Category coat = buildCategory(4L, 3L, "COAT", "外套", 3);

        when(categoryRepository.selectAll()).thenReturn(List.of(product, fabric, women, coat));

        List<Category> tree = categoryDomainService.getCategoryTree();

        // 应有2个顶级节点
        assertEquals(2, tree.size());

        // 成品下有1个子节点（女装）
        Category productNode = tree.stream()
                .filter(c -> c.getCode().equals("PRODUCT")).findFirst().orElseThrow();
        assertEquals(1, productNode.getChildren().size());
        assertEquals("WOMEN", productNode.getChildren().get(0).getCode());

        // 女装下有1个子节点（外套）
        Category womenNode = productNode.getChildren().get(0);
        assertEquals(1, womenNode.getChildren().size());
        assertEquals("COAT", womenNode.getChildren().get(0).getCode());

        // 面料下无子节点
        Category fabricNode = tree.stream()
                .filter(c -> c.getCode().equals("FABRIC")).findFirst().orElseThrow();
        assertTrue(fabricNode.getChildren().isEmpty());
    }

    @Test
    @DisplayName("分类树 — 空列表返回空树")
    void getCategoryTree_emptyList_shouldReturnEmpty() {
        when(categoryRepository.selectAll()).thenReturn(List.of());

        List<Category> tree = categoryDomainService.getCategoryTree();

        assertTrue(tree.isEmpty());
    }

    @Test
    @DisplayName("分类树 — 仅有顶级节点时无 children")
    void getCategoryTree_onlyTopLevel_noChildren() {
        Category product = buildCategory(1L, null, "PRODUCT", "成品", 1);
        Category fabric = buildCategory(2L, null, "FABRIC", "面料", 1);

        when(categoryRepository.selectAll()).thenReturn(List.of(product, fabric));

        List<Category> tree = categoryDomainService.getCategoryTree();

        assertEquals(2, tree.size());
        assertTrue(tree.get(0).getChildren().isEmpty());
        assertTrue(tree.get(1).getChildren().isEmpty());
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建分类实体（测试辅助方法）
     */
    private Category buildCategory(Long id, Long parentId, String code, String name, int level) {
        Category category = new Category();
        category.setId(id);
        category.setParentId(parentId);
        category.setCode(code);
        category.setName(name);
        category.setLevel(level);
        category.setSortOrder(0);
        category.setStatus(CommonStatus.ACTIVE);
        return category;
    }
}
