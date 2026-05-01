package com.jingwei.master;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.CommonStatus;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.SettlementType;
import com.jingwei.master.domain.model.Supplier;
import com.jingwei.master.domain.model.SupplierQualificationStatus;
import com.jingwei.master.domain.model.SupplierType;
import com.jingwei.master.domain.repository.SupplierRepository;
import com.jingwei.master.domain.service.SupplierDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SupplierDomainService 单元测试
 * <p>
 * 测试供应商领域服务的核心业务规则：
 * <ul>
 *   <li>创建供应商：编码非空校验、编码唯一性、名称唯一性、默认状态和资质</li>
 *   <li>更新供应商：名称唯一性（排除自身）、编码和类型不可修改</li>
 *   <li>停用/启用供应商：状态幂等校验</li>
 *   <li>删除供应商：引用检查（预留钩子）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class SupplierDomainServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierDomainService supplierDomainService;

    // ==================== 创建供应商 ====================

    @Test
    @DisplayName("创建供应商 — 正常创建，默认 ACTIVE + PENDING")
    void createSupplier_shouldSetDefaultStatusAndQualification() {
        when(supplierRepository.existsByCode("SUP-000001")).thenReturn(false);
        when(supplierRepository.existsByName("华纺面料", null)).thenReturn(false);
        when(supplierRepository.insert(any())).thenReturn(1);

        Supplier supplier = buildSupplier("SUP-000001", "华纺面料", SupplierType.FABRIC);
        Supplier result = supplierDomainService.createSupplier(supplier);

        assertEquals(CommonStatus.ACTIVE, result.getStatus());
        assertEquals(SupplierQualificationStatus.PENDING, result.getQualificationStatus());
    }

    @Test
    @DisplayName("创建供应商 — 显式设置资质状态应保留")
    void createSupplier_explicitQualification_shouldKeep() {
        when(supplierRepository.existsByCode("SUP-000002")).thenReturn(false);
        when(supplierRepository.existsByName("合格供应商", null)).thenReturn(false);
        when(supplierRepository.insert(any())).thenReturn(1);

        Supplier supplier = buildSupplier("SUP-000002", "合格供应商", SupplierType.TRIM);
        supplier.setQualificationStatus(SupplierQualificationStatus.QUALIFIED);
        Supplier result = supplierDomainService.createSupplier(supplier);

        assertEquals(SupplierQualificationStatus.QUALIFIED, result.getQualificationStatus());
    }

    @Test
    @DisplayName("创建供应商 — 编码为空应抛异常")
    void createSupplier_codeBlank_shouldThrow() {
        Supplier supplier = buildSupplier("", "华纺面料", SupplierType.FABRIC);

        BizException ex = assertThrows(BizException.class,
                () -> supplierDomainService.createSupplier(supplier));
        assertTrue(ex.getMessage().contains("供应商编码不能为空"));
    }

    @Test
    @DisplayName("创建供应商 — 编码为 null 应抛异常")
    void createSupplier_codeNull_shouldThrow() {
        Supplier supplier = buildSupplier(null, "华纺面料", SupplierType.FABRIC);

        BizException ex = assertThrows(BizException.class,
                () -> supplierDomainService.createSupplier(supplier));
        assertTrue(ex.getMessage().contains("供应商编码不能为空"));
    }

    @Test
    @DisplayName("创建供应商 — 编码重复应抛异常")
    void createSupplier_duplicateCode_shouldThrow() {
        when(supplierRepository.existsByCode("SUP-000001")).thenReturn(true);

        Supplier supplier = buildSupplier("SUP-000001", "新供应商", SupplierType.FABRIC);

        BizException ex = assertThrows(BizException.class,
                () -> supplierDomainService.createSupplier(supplier));
        assertTrue(ex.getMessage().contains("供应商编码已存在"));
    }

    @Test
    @DisplayName("创建供应商 — 名称重复应抛异常")
    void createSupplier_duplicateName_shouldThrow() {
        when(supplierRepository.existsByCode("SUP-000001")).thenReturn(false);
        when(supplierRepository.existsByName("华纺面料", null)).thenReturn(true);

        Supplier supplier = buildSupplier("SUP-000001", "华纺面料", SupplierType.FABRIC);

        BizException ex = assertThrows(BizException.class,
                () -> supplierDomainService.createSupplier(supplier));
        assertEquals(ErrorCode.SUPPLIER_NAME_DUPLICATE.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("创建供应商 — 并发唯一约束冲突应抛异常")
    void createSupplier_concurrentDuplicateKey_shouldThrow() {
        when(supplierRepository.existsByCode("SUP-000001")).thenReturn(false);
        when(supplierRepository.existsByName("华纺面料", null)).thenReturn(false);
        when(supplierRepository.insert(any())).thenThrow(new DuplicateKeyException("uk_md_supplier_code"));

        Supplier supplier = buildSupplier("SUP-000001", "华纺面料", SupplierType.FABRIC);

        BizException ex = assertThrows(BizException.class,
                () -> supplierDomainService.createSupplier(supplier));
        assertTrue(ex.getMessage().contains("供应商编码或名称已存在"));
    }

    // ==================== 更新供应商 ====================

    @Test
    @DisplayName("更新供应商 — 正常更新应成功")
    void updateSupplier_shouldSucceed() {
        Supplier existing = buildSavedSupplier(1L, "SUP-000001", "华纺面料", SupplierType.FABRIC);
        when(supplierRepository.selectById(1L)).thenReturn(existing);
        when(supplierRepository.updateById(any())).thenReturn(1);

        Supplier update = new Supplier();
        update.setName("华纺面料（更新）");
        update.setContactPerson("李四");

        // 名称与原名称不同，需要唯一性校验
        when(supplierRepository.existsByName("华纺面料（更新）", 1L)).thenReturn(false);

        Supplier result = supplierDomainService.updateSupplier(1L, update);

        // 验证编码和类型不可修改
        verify(supplierRepository).updateById(argThat(s ->
                s.getCode().equals("SUP-000001") && s.getType() == SupplierType.FABRIC));
    }

    @Test
    @DisplayName("更新供应商 — 名称修改时校验唯一性（排除自身）")
    void updateSupplier_changeName_duplicate_shouldThrow() {
        Supplier existing = buildSavedSupplier(1L, "SUP-000001", "华纺面料", SupplierType.FABRIC);
        when(supplierRepository.selectById(1L)).thenReturn(existing);
        when(supplierRepository.existsByName("重名供应商", 1L)).thenReturn(true);

        Supplier update = new Supplier();
        update.setName("重名供应商");

        BizException ex = assertThrows(BizException.class,
                () -> supplierDomainService.updateSupplier(1L, update));
        assertEquals(ErrorCode.SUPPLIER_NAME_DUPLICATE.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("更新供应商 — 名称不变时不触发唯一性校验")
    void updateSupplier_sameName_shouldSkipUniquenessCheck() {
        Supplier existing = buildSavedSupplier(1L, "SUP-000001", "华纺面料", SupplierType.FABRIC);
        when(supplierRepository.selectById(1L)).thenReturn(existing);
        when(supplierRepository.updateById(any())).thenReturn(1);

        Supplier update = new Supplier();
        update.setName("华纺面料");  // 同名
        update.setContactPerson("王五");

        supplierDomainService.updateSupplier(1L, update);

        // 名称不变，不应调用 existsByName
        verify(supplierRepository, never()).existsByName(anyString(), any());
    }

    @Test
    @DisplayName("更新供应商 — 不存在应抛异常")
    void updateSupplier_notFound_shouldThrow() {
        when(supplierRepository.selectById(999L)).thenReturn(null);

        Supplier update = new Supplier();
        update.setName("新名称");

        BizException ex = assertThrows(BizException.class,
                () -> supplierDomainService.updateSupplier(999L, update));
        assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("更新供应商 — 乐观锁冲突应抛异常")
    void updateSupplier_concurrentConflict_shouldThrow() {
        Supplier existing = buildSavedSupplier(1L, "SUP-000001", "华纺面料", SupplierType.FABRIC);
        when(supplierRepository.selectById(1L)).thenReturn(existing);
        when(supplierRepository.updateById(any())).thenReturn(0);

        Supplier update = new Supplier();
        update.setContactPerson("李四");

        BizException ex = assertThrows(BizException.class,
                () -> supplierDomainService.updateSupplier(1L, update));
        assertEquals(ErrorCode.CONCURRENT_CONFLICT.getCode(), ex.getCode());
    }

    // ==================== 停用/启用供应商 ====================

    @Test
    @DisplayName("停用供应商 — 成功")
    void deactivateSupplier_shouldSucceed() {
        Supplier existing = buildSavedSupplier(1L, "SUP-000001", "华纺面料", SupplierType.FABRIC);
        when(supplierRepository.selectById(1L)).thenReturn(existing);
        when(supplierRepository.updateById(any())).thenReturn(1);

        supplierDomainService.deactivateSupplier(1L);

        assertEquals(CommonStatus.INACTIVE, existing.getStatus());
    }

    @Test
    @DisplayName("停用供应商 — 已停用再停用应抛异常")
    void deactivateSupplier_alreadyInactive_shouldThrow() {
        Supplier existing = buildSavedSupplier(1L, "SUP-000001", "华纺面料", SupplierType.FABRIC);
        existing.setStatus(CommonStatus.INACTIVE);
        when(supplierRepository.selectById(1L)).thenReturn(existing);

        BizException ex = assertThrows(BizException.class,
                () -> supplierDomainService.deactivateSupplier(1L));
        assertTrue(ex.getMessage().contains("供应商已停用"));
    }

    @Test
    @DisplayName("启用供应商 — 成功")
    void activateSupplier_shouldSucceed() {
        Supplier existing = buildSavedSupplier(1L, "SUP-000001", "华纺面料", SupplierType.FABRIC);
        existing.setStatus(CommonStatus.INACTIVE);
        when(supplierRepository.selectById(1L)).thenReturn(existing);
        when(supplierRepository.updateById(any())).thenReturn(1);

        supplierDomainService.activateSupplier(1L);

        assertEquals(CommonStatus.ACTIVE, existing.getStatus());
    }

    @Test
    @DisplayName("启用供应商 — 已启用再启用应抛异常")
    void activateSupplier_alreadyActive_shouldThrow() {
        Supplier existing = buildSavedSupplier(1L, "SUP-000001", "华纺面料", SupplierType.FABRIC);
        when(supplierRepository.selectById(1L)).thenReturn(existing);

        BizException ex = assertThrows(BizException.class,
                () -> supplierDomainService.activateSupplier(1L));
        assertTrue(ex.getMessage().contains("供应商已启用"));
    }

    // ==================== 删除供应商 ====================

    @Test
    @DisplayName("删除供应商 — 无引用应成功")
    void deleteSupplier_noReference_shouldSucceed() {
        Supplier existing = buildSavedSupplier(1L, "SUP-000001", "华纺面料", SupplierType.FABRIC);
        when(supplierRepository.selectById(1L)).thenReturn(existing);
        when(supplierRepository.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> supplierDomainService.deleteSupplier(1L));
        verify(supplierRepository).deleteById(1L);
    }

    @Test
    @DisplayName("删除供应商 — 不存在应抛异常")
    void deleteSupplier_notFound_shouldThrow() {
        when(supplierRepository.selectById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> supplierDomainService.deleteSupplier(999L));
        assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
    }

    // ==================== 查询供应商 ====================

    @Test
    @DisplayName("查询供应商详情 — 不存在应抛异常")
    void getSupplierById_notFound_shouldThrow() {
        when(supplierRepository.selectById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> supplierDomainService.getSupplierById(999L));
        assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("查询供应商详情 — 存在应返回实体")
    void getSupplierById_shouldReturn() {
        Supplier existing = buildSavedSupplier(1L, "SUP-000001", "华纺面料", SupplierType.FABRIC);
        when(supplierRepository.selectById(1L)).thenReturn(existing);

        Supplier result = supplierDomainService.getSupplierById(1L);

        assertEquals("SUP-000001", result.getCode());
        assertEquals("华纺面料", result.getName());
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建未保存的供应商实体（模拟创建请求）
     */
    private Supplier buildSupplier(String code, String name, SupplierType type) {
        Supplier supplier = new Supplier();
        supplier.setCode(code);
        supplier.setName(name);
        supplier.setType(type);
        supplier.setContactPerson("张三");
        supplier.setContactPhone("13800138000");
        supplier.setSettlementType(SettlementType.MONTHLY);
        supplier.setLeadTimeDays(15);
        return supplier;
    }

    /**
     * 构建已保存的供应商实体（模拟数据库查询结果）
     */
    private Supplier buildSavedSupplier(Long id, String code, String name, SupplierType type) {
        Supplier supplier = buildSupplier(code, name, type);
        supplier.setId(id);
        supplier.setStatus(CommonStatus.ACTIVE);
        supplier.setQualificationStatus(SupplierQualificationStatus.PENDING);
        return supplier;
    }
}
