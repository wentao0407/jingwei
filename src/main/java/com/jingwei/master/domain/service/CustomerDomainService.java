package com.jingwei.master.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.CommonStatus;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.Customer;
import com.jingwei.master.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 客户领域服务
 * <p>
 * 核心职责：
 * <ul>
 *   <li>客户 CRUD 及业务校验</li>
 *   <li>名称唯一性校验（全局唯一，不允许同名客户）</li>
 *   <li>客户停用控制（停用后不可创建新销售订单，已有订单不受影响）</li>
 * </ul>
 * </p>
 * <p>
 * 关键业务规则：
 * <ul>
 *   <li>客户编码由编码规则引擎自动生成，不可手动指定</li>
 *   <li>客户名称全局唯一——服装行业以客户名称为主标识</li>
 *   <li>客户等级影响定价折扣（A客户95折、B客户9折等，在价格表中配置），本模块仅存储等级</li>
 *   <li>信用额度为参考值，本期不做超额拦截（P2扩展）</li>
 *   <li>停用（INACTIVE）客户不可创建新销售订单——停用是客户关系的终止信号</li>
 *   <li>客户类型不可修改——类型影响定价策略和销售渠道，变更会导致历史数据不一致</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerDomainService {

    private final CustomerRepository customerRepository;

    /**
     * 获取客户仓库引用（供 ApplicationService 分页查询使用）
     *
     * @return 客户仓库
     */
    public CustomerRepository getCustomerRepository() {
        return customerRepository;
    }

    /**
     * 创建客户
     * <p>
     * 校验规则：
     * <ol>
     *   <li>客户编码由编码规则引擎生成，不可为空</li>
     *   <li>客户编码不可重复（应用层校验 + 数据库唯一索引兜底）</li>
     *   <li>客户名称全局唯一</li>
     * </ol>
     * </p>
     *
     * @param customer 客户实体（code 应由调用方从编码规则引擎获取后设置）
     * @return 保存后的客户实体
     */
    public Customer createCustomer(Customer customer) {
        // 编码不可为空（应由编码规则引擎生成）
        if (customer.getCode() == null || customer.getCode().isBlank()) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "客户编码不能为空");
        }

        // 编码唯一性校验
        if (customerRepository.existsByCode(customer.getCode())) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "客户编码已存在");
        }

        // 名称唯一性校验
        if (customerRepository.existsByName(customer.getName(), null)) {
            throw new BizException(ErrorCode.CUSTOMER_NAME_DUPLICATE);
        }

        // 新建客户默认为 ACTIVE 状态
        customer.setStatus(CommonStatus.ACTIVE);

        try {
            customerRepository.insert(customer);
        } catch (DuplicateKeyException e) {
            log.warn("并发创建客户触发唯一约束: code={}", customer.getCode());
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "客户编码或名称已存在");
        }

        log.info("创建客户: code={}, name={}, type={}, id={}",
                customer.getCode(), customer.getName(), customer.getType(), customer.getId());
        return customer;
    }

    /**
     * 更新客户
     * <p>
     * 可更新字段：name, shortName, level, contactPerson, contactPhone, address,
     * deliveryAddress, settlementType, creditLimit, remark。
     * 客户编码（code）和类型（type）不可修改——编码被销售订单等业务单据引用，
     * 类型影响定价策略和销售渠道，变更会导致历史数据不一致。
     * </p>
     *
     * @param customerId 客户ID
     * @param customer   包含更新字段的客户实体
     * @return 更新后的客户
     */
    public Customer updateCustomer(Long customerId, Customer customer) {
        Customer existing = customerRepository.selectById(customerId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "客户不存在");
        }

        // 名称唯一性校验（排除自身）
        if (customer.getName() != null && !customer.getName().equals(existing.getName())) {
            if (customerRepository.existsByName(customer.getName(), customerId)) {
                throw new BizException(ErrorCode.CUSTOMER_NAME_DUPLICATE);
            }
        }

        // 编码和类型不可修改
        customer.setId(customerId);
        customer.setCode(existing.getCode());
        customer.setType(existing.getType());

        int rows = customerRepository.updateById(customer);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新客户: id={}", customerId);
        return customerRepository.selectById(customerId);
    }

    /**
     * 停用客户
     * <p>
     * 停用后不可创建新销售订单，已有订单不受影响。
     * 已停用的客户不允许重复停用。
     * </p>
     *
     * @param customerId 客户ID
     */
    public void deactivateCustomer(Long customerId) {
        Customer existing = customerRepository.selectById(customerId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "客户不存在");
        }

        if (existing.getStatus() == CommonStatus.INACTIVE) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "客户已停用");
        }

        existing.setStatus(CommonStatus.INACTIVE);
        int rows = customerRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("停用客户: id={}, code={}", customerId, existing.getCode());
    }

    /**
     * 启用客户
     * <p>
     * 将停用的客户重新启用，启用后可创建新销售订单。
     * 已启用的客户不允许重复启用。
     * </p>
     *
     * @param customerId 客户ID
     */
    public void activateCustomer(Long customerId) {
        Customer existing = customerRepository.selectById(customerId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "客户不存在");
        }

        if (existing.getStatus() == CommonStatus.ACTIVE) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "客户已启用");
        }

        existing.setStatus(CommonStatus.ACTIVE);
        int rows = customerRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("启用客户: id={}, code={}", customerId, existing.getCode());
    }

    /**
     * 删除客户
     * <p>
     * 仅允许删除未被业务单据引用的客户。
     * 当前销售订单模块尚未实现，预留引用检查钩子。
     * </p>
     *
     * @param customerId 客户ID
     */
    public void deleteCustomer(Long customerId) {
        Customer existing = customerRepository.selectById(customerId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "客户不存在");
        }

        // 检查是否被销售订单引用（当前订单模块尚未实现，预留钩子）
        long orderCount = countOrderReferences(customerId);
        if (orderCount > 0) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "该客户已被" + orderCount + "个销售订单引用，不可删除");
        }

        customerRepository.deleteById(customerId);
        log.info("删除客户: id={}, code={}", customerId, existing.getCode());
    }

    /**
     * 根据ID查询客户详情
     *
     * @param customerId 客户ID
     * @return 客户实体
     */
    public Customer getCustomerById(Long customerId) {
        Customer customer = customerRepository.selectById(customerId);
        if (customer == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "客户不存在");
        }
        return customer;
    }

    // ==================== 私有方法 ====================

    /**
     * 统计引用该客户的销售订单数量
     * <p>
     * 当前订单模块尚未实现，返回 0。
     * 订单模块实现后，应替换为真实查询。
     * </p>
     *
     * @param customerId 客户ID
     * @return 引用该客户的销售订单数量
     */
    private long countOrderReferences(Long customerId) {
        // TODO: 订单模块实现后，注入销售订单 Mapper 并查询真实引用数量
        return 0;
    }
}
