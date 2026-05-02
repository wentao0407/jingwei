package com.jingwei.master.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.master.application.dto.CreateCustomerDTO;
import com.jingwei.master.application.dto.CustomerQueryDTO;
import com.jingwei.master.application.dto.UpdateCustomerDTO;
import com.jingwei.master.application.service.CustomerApplicationService;
import com.jingwei.master.interfaces.vo.CustomerVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户管理 Controller
 * <p>
 * 提供客户档案的 CRUD 接口。
 * 所有接口统一使用 POST 方法。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerApplicationService customerApplicationService;

    /**
     * 创建客户
     * <p>
     * 编码由编码规则引擎自动生成（格式：CUS-000001），
     * 状态默认 ACTIVE，等级默认 C。
     * </p>
     */
    @RequirePermission("master:customer:create")
    @PostMapping("/master/customer/create")
    public R<CustomerVO> createCustomer(@Valid @RequestBody CreateCustomerDTO dto) {
        return R.ok(customerApplicationService.createCustomer(dto));
    }

    /**
     * 更新客户
     * <p>
     * 可更新字段：name, shortName, level, contactPerson, contactPhone, address,
     * deliveryAddress, settlementType, creditLimit, remark。
     * 编码和类型不可修改。
     * </p>
     */
    @RequirePermission("master:customer:update")
    @PostMapping("/master/customer/update")
    public R<CustomerVO> updateCustomer(@RequestParam Long customerId,
                                        @Valid @RequestBody UpdateCustomerDTO dto) {
        return R.ok(customerApplicationService.updateCustomer(customerId, dto));
    }

    /**
     * 停用客户
     * <p>
     * 停用后不可创建新销售订单，已有订单不受影响。
     * </p>
     */
    @RequirePermission("master:customer:deactivate")
    @PostMapping("/master/customer/deactivate")
    public R<Void> deactivateCustomer(@RequestParam Long customerId) {
        customerApplicationService.deactivateCustomer(customerId);
        return R.ok();
    }

    /**
     * 启用客户
     * <p>
     * 将停用的客户重新启用，启用后可创建新销售订单。
     * </p>
     */
    @RequirePermission("master:customer:activate")
    @PostMapping("/master/customer/activate")
    public R<Void> activateCustomer(@RequestParam Long customerId) {
        customerApplicationService.activateCustomer(customerId);
        return R.ok();
    }

    /**
     * 删除客户
     * <p>
     * 仅允许删除未被销售订单引用的客户。
     * </p>
     */
    @RequirePermission("master:customer:delete")
    @PostMapping("/master/customer/delete")
    public R<Void> deleteCustomer(@RequestParam Long customerId) {
        customerApplicationService.deleteCustomer(customerId);
        return R.ok();
    }

    /**
     * 查询客户详情
     */
    @PostMapping("/master/customer/detail")
    public R<CustomerVO> getCustomerDetail(@RequestParam Long customerId) {
        return R.ok(customerApplicationService.getCustomerById(customerId));
    }

    /**
     * 分页查询客户
     * <p>
     * 支持按类型、等级、状态筛选，支持按编码或名称关键词搜索。
     * </p>
     */
    @PostMapping("/master/customer/page")
    public R<IPage<CustomerVO>> pageQuery(@Valid @RequestBody CustomerQueryDTO dto) {
        return R.ok(customerApplicationService.pageQuery(dto));
    }
}
