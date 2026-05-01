package com.jingwei.master;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jingwei.master.domain.model.Material;
import com.jingwei.master.infrastructure.persistence.CategoryMapper;
import com.jingwei.master.infrastructure.persistence.CategoryRepositoryImpl;
import com.jingwei.master.infrastructure.persistence.MaterialMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CategoryRepositoryImpl.countMaterialsByCategoryId 测试
 * <p>
 * 验证分类删除保护的物料引用计数查询：
 * <ul>
 *   <li>有物料引用时返回正确的数量</li>
 *   <li>无物料引用时返回 0</li>
 * </ul>
 * T-09 合入后，此查询使用真实的 MaterialMapper，端到端保护已生效。
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class CategoryRepositoryImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private MaterialMapper materialMapper;

    @InjectMocks
    private CategoryRepositoryImpl categoryRepository;

    @Test
    @DisplayName("物料引用计数 — 有引用时返回正确数量")
    void countMaterials_withReferences_returnsCount() {
        when(materialMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        long count = categoryRepository.countMaterialsByCategoryId(1L);

        assertEquals(3L, count);
    }

    @Test
    @DisplayName("物料引用计数 — 无引用时返回0")
    void countMaterials_noReferences_returns0() {
        when(materialMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        long count = categoryRepository.countMaterialsByCategoryId(1L);

        assertEquals(0L, count);
    }
}
