package com.jingwei.master;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.CommonStatus;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.*;
import com.jingwei.master.domain.repository.CustomerRepository;
import com.jingwei.master.domain.service.CustomerDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CustomerDomainService 单元测试
 * <p>
 * 测试客户领域服务的核心业务规则：
 * <ul>
 *   <li>创建客户：编码非空校验、编码唯一性、名称唯一性、默认状态</li>
 *   <li>更新客户：名称唯一性（排除自身）、编码和类型不可修改</li>
 *   <li>停用/启用客户：状态幂等校验</li>
 *   <li>删除客户：引用检查（预留钩子）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class CustomerDomainServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerDomainService customerDomainService;

    // ==================== 创建客户 ====================

    @Test
    @DisplayName("创建客户 — 正常创建，默认 ACTIVE")
    void createCustomer_shouldSetDefaultStatus() {
        when(customerRepository.existsByCode("CUS-000001")).thenReturn(false);
        when(customerRepository.existsByName("优衣库", null)).thenReturn(false);
        when(customerRepository.insert(any())).thenReturn(1);

        Customer customer = buildCustomer("CUS-000001", "优衣库", CustomerType.WHOLESALE);
        Customer result = customerDomainService.createCustomer(customer);

        assertEquals(CommonStatus.ACTIVE, result.getStatus());
    }

    @Test
    @DisplayName("创建客户 — 含等级和信用额度")
    void createCustomer_withLevelAndCreditLimit() {
        when(customerRepository.existsByCode("CUS-000002")).thenReturn(false);
        when(customerRepository.existsByName("A级客户", null)).thenReturn(false);
        when(customerRepository.insert(any())).thenReturn(1);

        Customer customer = buildCustomer("CUS-000002", "A级客户", CustomerType.FRANCHISE);
        customer.setLevel(CustomerLevel.A);
        customer.setCreditLimit(new BigDecimal("500000.00"));
        Customer result = customerDomainService.createCustomer(customer);

        assertEquals(CustomerLevel.A, result.getLevel());
        assertEquals(0, new BigDecimal("500000.00").compareTo(result.getCreditLimit()));
    }

    @Test
    @DisplayName("创建客户 — 编码为空应抛异常")
    void createCustomer_codeBlank_shouldThrow() {
        Customer customer = buildCustomer("", "优衣库", CustomerType.WHOLESALE);

        BizException ex = assertThrows(BizException.class,
                () -> customerDomainService.createCustomer(customer));
        assertTrue(ex.getMessage().contains("客户编码不能为空"));
    }

    @Test
    @DisplayName("创建客户 — 编码为 null 应抛异常")
    void createCustomer_codeNull_shouldThrow() {
        Customer customer = buildCustomer(null, "优衣库", CustomerType.WHOLESALE);

        BizException ex = assertThrows(BizException.class,
                () -> customerDomainService.createCustomer(customer));
        assertTrue(ex.getMessage().contains("客户编码不能为空"));
    }

    @Test
    @DisplayName("创建客户 — 编码重复应抛异常")
    void createCustomer_duplicateCode_shouldThrow() {
        when(customerRepository.existsByCode("CUS-000001")).thenReturn(true);

        Customer customer = buildCustomer("CUS-000001", "新客户", CustomerType.WHOLESALE);

        BizException ex = assertThrows(BizException.class,
                () -> customerDomainService.createCustomer(customer));
        assertTrue(ex.getMessage().contains("客户编码已存在"));
    }

    @Test
    @DisplayName("创建客户 — 名称重复应抛异常")
    void createCustomer_duplicateName_shouldThrow() {
        when(customerRepository.existsByCode("CUS-000001")).thenReturn(false);
        when(customerRepository.existsByName("优衣库", null)).thenReturn(true);

        Customer customer = buildCustomer("CUS-000001", "优衣库", CustomerType.WHOLESALE);

        BizException ex = assertThrows(BizException.class,
                () -> customerDomainService.createCustomer(customer));
        assertEquals(ErrorCode.CUSTOMER_NAME_DUPLICATE.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("创建客户 — 并发唯一约束冲突应抛异常")
    void createCustomer_concurrentDuplicateKey_shouldThrow() {
        when(customerRepository.existsByCode("CUS-000001")).thenReturn(false);
        when(customerRepository.existsByName("优衣库", null)).thenReturn(false);
        when(customerRepository.insert(any())).thenThrow(new DuplicateKeyException("uk_md_customer_name"));

        Customer customer = buildCustomer("CUS-000001", "优衣库", CustomerType.WHOLESALE);

        BizException ex = assertThrows(BizException.class,
                () -> customerDomainService.createCustomer(customer));
        assertTrue(ex.getMessage().contains("客户编码或名称已存在"));
    }

    // ==================== 更新客户 ====================

    @Test
    @DisplayName("更新客户 — 正常更新应成功")
    void updateCustomer_shouldSucceed() {
        Customer existing = buildSavedCustomer(1L, "CUS-000001", "优衣库", CustomerType.WHOLESALE);
        when(customerRepository.selectById(1L)).thenReturn(existing);
        when(customerRepository.updateById(any())).thenReturn(1);

        Customer update = new Customer();
        update.setName("优衣库（更新）");
        update.setLevel(CustomerLevel.B);

        // 名称与原名称不同，需要唯一性校验
        when(customerRepository.existsByName("优衣库（更新）", 1L)).thenReturn(false);

        customerDomainService.updateCustomer(1L, update);

        // 验证编码和类型不可修改
        verify(customerRepository).updateById(argThat(c ->
                c.getCode().equals("CUS-000001") && c.getType() == CustomerType.WHOLESALE));
    }

    @Test
    @DisplayName("更新客户 — 名称修改时校验唯一性（排除自身）")
    void updateCustomer_changeName_duplicate_shouldThrow() {
        Customer existing = buildSavedCustomer(1L, "CUS-000001", "优衣库", CustomerType.WHOLESALE);
        when(customerRepository.selectById(1L)).thenReturn(existing);
        when(customerRepository.existsByName("重名客户", 1L)).thenReturn(true);

        Customer update = new Customer();
        update.setName("重名客户");

        BizException ex = assertThrows(BizException.class,
                () -> customerDomainService.updateCustomer(1L, update));
        assertEquals(ErrorCode.CUSTOMER_NAME_DUPLICATE.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("更新客户 — 名称不变时不触发唯一性校验")
    void updateCustomer_sameName_shouldSkipUniquenessCheck() {
        Customer existing = buildSavedCustomer(1L, "CUS-000001", "优衣库", CustomerType.WHOLESALE);
        when(customerRepository.selectById(1L)).thenReturn(existing);
        when(customerRepository.updateById(any())).thenReturn(1);

        Customer update = new Customer();
        update.setName("优衣库");  // 同名
        update.setContactPerson("王五");

        customerDomainService.updateCustomer(1L, update);

        // 名称不变，不应调用 existsByName
        verify(customerRepository, never()).existsByName(anyString(), any());
    }

    @Test
    @DisplayName("更新客户 — 不存在应抛异常")
    void updateCustomer_notFound_shouldThrow() {
        when(customerRepository.selectById(999L)).thenReturn(null);

        Customer update = new Customer();
        update.setName("新名称");

        BizException ex = assertThrows(BizException.class,
                () -> customerDomainService.updateCustomer(999L, update));
        assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("更新客户 — 乐观锁冲突应抛异常")
    void updateCustomer_concurrentConflict_shouldThrow() {
        Customer existing = buildSavedCustomer(1L, "CUS-000001", "优衣库", CustomerType.WHOLESALE);
        when(customerRepository.selectById(1L)).thenReturn(existing);
        when(customerRepository.updateById(any())).thenReturn(0);

        Customer update = new Customer();
        update.setContactPerson("李四");

        BizException ex = assertThrows(BizException.class,
                () -> customerDomainService.updateCustomer(1L, update));
        assertEquals(ErrorCode.CONCURRENT_CONFLICT.getCode(), ex.getCode());
    }

    // ==================== 停用/启用客户 ====================

    @Test
    @DisplayName("停用客户 — 成功")
    void deactivateCustomer_shouldSucceed() {
        Customer existing = buildSavedCustomer(1L, "CUS-000001", "优衣库", CustomerType.WHOLESALE);
        when(customerRepository.selectById(1L)).thenReturn(existing);
        when(customerRepository.updateById(any())).thenReturn(1);

        customerDomainService.deactivateCustomer(1L);

        assertEquals(CommonStatus.INACTIVE, existing.getStatus());
    }

    @Test
    @DisplayName("停用客户 — 已停用再停用应抛异常")
    void deactivateCustomer_alreadyInactive_shouldThrow() {
        Customer existing = buildSavedCustomer(1L, "CUS-000001", "优衣库", CustomerType.WHOLESALE);
        existing.setStatus(CommonStatus.INACTIVE);
        when(customerRepository.selectById(1L)).thenReturn(existing);

        BizException ex = assertThrows(BizException.class,
                () -> customerDomainService.deactivateCustomer(1L));
        assertTrue(ex.getMessage().contains("客户已停用"));
    }

    @Test
    @DisplayName("启用客户 — 成功")
    void activateCustomer_shouldSucceed() {
        Customer existing = buildSavedCustomer(1L, "CUS-000001", "优衣库", CustomerType.WHOLESALE);
        existing.setStatus(CommonStatus.INACTIVE);
        when(customerRepository.selectById(1L)).thenReturn(existing);
        when(customerRepository.updateById(any())).thenReturn(1);

        customerDomainService.activateCustomer(1L);

        assertEquals(CommonStatus.ACTIVE, existing.getStatus());
    }

    @Test
    @DisplayName("启用客户 — 已启用再启用应抛异常")
    void activateCustomer_alreadyActive_shouldThrow() {
        Customer existing = buildSavedCustomer(1L, "CUS-000001", "优衣库", CustomerType.WHOLESALE);
        when(customerRepository.selectById(1L)).thenReturn(existing);

        BizException ex = assertThrows(BizException.class,
                () -> customerDomainService.activateCustomer(1L));
        assertTrue(ex.getMessage().contains("客户已启用"));
    }

    // ==================== 删除客户 ====================

    @Test
    @DisplayName("删除客户 — 无引用应成功")
    void deleteCustomer_noReference_shouldSucceed() {
        Customer existing = buildSavedCustomer(1L, "CUS-000001", "优衣库", CustomerType.WHOLESALE);
        when(customerRepository.selectById(1L)).thenReturn(existing);
        when(customerRepository.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> customerDomainService.deleteCustomer(1L));
        verify(customerRepository).deleteById(1L);
    }

    @Test
    @DisplayName("删除客户 — 不存在应抛异常")
    void deleteCustomer_notFound_shouldThrow() {
        when(customerRepository.selectById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> customerDomainService.deleteCustomer(999L));
        assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
    }

    // ==================== 查询客户 ====================

    @Test
    @DisplayName("查询客户详情 — 不存在应抛异常")
    void getCustomerById_notFound_shouldThrow() {
        when(customerRepository.selectById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> customerDomainService.getCustomerById(999L));
        assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("查询客户详情 — 存在应返回实体")
    void getCustomerById_shouldReturn() {
        Customer existing = buildSavedCustomer(1L, "CUS-000001", "优衣库", CustomerType.WHOLESALE);
        when(customerRepository.selectById(1L)).thenReturn(existing);

        Customer result = customerDomainService.getCustomerById(1L);

        assertEquals("CUS-000001", result.getCode());
        assertEquals("优衣库", result.getName());
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建未保存的客户实体（模拟创建请求）
     */
    private Customer buildCustomer(String code, String name, CustomerType type) {
        Customer customer = new Customer();
        customer.setCode(code);
        customer.setName(name);
        customer.setType(type);
        customer.setLevel(CustomerLevel.C);
        customer.setContactPerson("张三");
        customer.setContactPhone("13900139000");
        customer.setSettlementType(SettlementType.MONTHLY);
        return customer;
    }

    /**
     * 构建已保存的客户实体（模拟数据库查询结果）
     */
    private Customer buildSavedCustomer(Long id, String code, String name, CustomerType type) {
        Customer customer = buildCustomer(code, name, type);
        customer.setId(id);
        customer.setStatus(CommonStatus.ACTIVE);
        return customer;
    }
}
