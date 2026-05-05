package com.jingwei.master.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.master.application.dto.CreateCustomerDTO;
import com.jingwei.master.application.dto.CustomerQueryDTO;
import com.jingwei.master.application.dto.UpdateCustomerDTO;
import com.jingwei.master.domain.model.*;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.master.domain.service.CustomerDomainService;
import com.jingwei.master.interfaces.vo.CustomerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 客户应用服务
 * <p>
 * 负责客户 CRUD 的编排和事务边界管理。
 * 客户编码生成调用编码规则引擎（CodingRuleDomainService），
 * 业务校验委托给 CustomerDomainService。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerApplicationService {

    /** 编码规则键：客户编码（格式 CUS-6位流水号），与 t_md_coding_rule 的 rule_code 对应 */
    private static final String CUSTOMER_CODE_RULE = "CUSTOMER_CODE";

    private final CustomerDomainService customerDomainService;
    private final CodingRuleDomainService codingRuleDomainService;

    /**
     * 创建客户
     * <p>
     * 编排流程：
     * <ol>
     *   <li>调用编码规则引擎生成客户编码</li>
     *   <li>组装 Customer 实体</li>
     *   <li>调用 DomainService 执行业务校验和持久化</li>
     * </ol>
     * </p>
     *
     * @param dto 创建请求
     * @return 客户 VO
     */
    @Transactional(rollbackFor = Exception.class)
    public CustomerVO createCustomer(CreateCustomerDTO dto) {
        // 1. 调用编码规则引擎生成客户编码
        String code = codingRuleDomainService.generateCode(CUSTOMER_CODE_RULE, Map.of());

        // 2. 组装实体
        Customer customer = new Customer();
        customer.setCode(code);
        customer.setName(dto.getName());
        customer.setShortName(dto.getShortName());
        customer.setType(CustomerType.valueOf(dto.getType()));
        if (dto.getLevel() != null) {
            customer.setLevel(CustomerLevel.valueOf(dto.getLevel()));
        } else {
            customer.setLevel(CustomerLevel.C);
        }
        customer.setContactPerson(dto.getContactPerson());
        customer.setContactPhone(dto.getContactPhone());
        customer.setAddress(dto.getAddress());
        customer.setDeliveryAddress(dto.getDeliveryAddress());
        if (dto.getSettlementType() != null) {
            customer.setSettlementType(SettlementType.valueOf(dto.getSettlementType()));
        }
        customer.setCreditLimit(dto.getCreditLimit());
        customer.setRemark(dto.getRemark() != null ? dto.getRemark() : "");

        // 3. 业务校验和持久化
        Customer saved = customerDomainService.createCustomer(customer);
        return toCustomerVO(saved);
    }

    /**
     * 更新客户
     * <p>
     * 可更新字段：name, shortName, level, contactPerson, contactPhone, address,
     * deliveryAddress, settlementType, creditLimit, remark。
     * 编码和类型不可修改。
     * </p>
     *
     * @param customerId 客户ID
     * @param dto        更新请求
     * @return 客户 VO
     */
    @Transactional(rollbackFor = Exception.class)
    public CustomerVO updateCustomer(Long customerId, UpdateCustomerDTO dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setShortName(dto.getShortName());
        if (dto.getLevel() != null) {
            customer.setLevel(CustomerLevel.valueOf(dto.getLevel()));
        }
        customer.setContactPerson(dto.getContactPerson());
        customer.setContactPhone(dto.getContactPhone());
        customer.setAddress(dto.getAddress());
        customer.setDeliveryAddress(dto.getDeliveryAddress());
        if (dto.getSettlementType() != null) {
            customer.setSettlementType(SettlementType.valueOf(dto.getSettlementType()));
        }
        customer.setCreditLimit(dto.getCreditLimit());
        customer.setRemark(dto.getRemark());

        Customer updated = customerDomainService.updateCustomer(customerId, customer);
        return toCustomerVO(updated);
    }

    /**
     * 停用客户
     * <p>
     * 停用后不可创建新销售订单，已有订单不受影响。
     * </p>
     *
     * @param customerId 客户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deactivateCustomer(Long customerId) {
        customerDomainService.deactivateCustomer(customerId);
    }

    /**
     * 启用客户
     * <p>
     * 将停用的客户重新启用，启用后可创建新销售订单。
     * </p>
     *
     * @param customerId 客户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void activateCustomer(Long customerId) {
        customerDomainService.activateCustomer(customerId);
    }

    /**
     * 删除客户
     * <p>
     * 仅允许删除未被销售订单引用的客户。
     * </p>
     *
     * @param customerId 客户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCustomer(Long customerId) {
        customerDomainService.deleteCustomer(customerId);
    }

    /**
     * 根据ID查询客户详情
     *
     * @param customerId 客户ID
     * @return 客户 VO
     */
    public CustomerVO getCustomerById(Long customerId) {
        Customer customer = customerDomainService.getCustomerById(customerId);
        return toCustomerVO(customer);
    }

    /**
     * 分页查询客户
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    public IPage<CustomerVO> pageQuery(CustomerQueryDTO dto) {
        Page<Customer> page = new Page<>(dto.getCurrent(), dto.getSize());

        IPage<Customer> customerPage = customerDomainService.getCustomerRepository()
                .selectPage(page, dto.getType(), dto.getLevel(),
                        dto.getStatus(), dto.getKeyword());

        return customerPage.convert(this::toCustomerVO);
    }

    // ==================== 转换方法 ====================

    /**
     * 将 Customer 实体转换为 CustomerVO
     *
     * @param customer 客户实体
     * @return 客户 VO
     */
    private CustomerVO toCustomerVO(Customer customer) {
        CustomerVO vo = new CustomerVO();
        vo.setId(customer.getId());
        vo.setCode(customer.getCode());
        vo.setName(customer.getName());
        vo.setShortName(customer.getShortName());
        vo.setType(customer.getType() != null ? customer.getType().name() : null);
        vo.setLevel(customer.getLevel() != null ? customer.getLevel().name() : null);
        vo.setContactPerson(customer.getContactPerson());
        vo.setContactPhone(customer.getContactPhone());
        vo.setAddress(customer.getAddress());
        vo.setDeliveryAddress(customer.getDeliveryAddress());
        vo.setSettlementType(customer.getSettlementType() != null ? customer.getSettlementType().name() : null);
        vo.setCreditLimit(customer.getCreditLimit());
        vo.setStatus(customer.getStatus() != null ? customer.getStatus().name() : null);
        vo.setRemark(customer.getRemark());
        vo.setCreatedAt(customer.getCreatedAt());
        vo.setUpdatedAt(customer.getUpdatedAt());
        return vo;
    }
}
