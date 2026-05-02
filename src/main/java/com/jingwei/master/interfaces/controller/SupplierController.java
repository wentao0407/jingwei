package com.jingwei.master.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.master.application.dto.CreateSupplierDTO;
import com.jingwei.master.application.dto.SupplierQueryDTO;
import com.jingwei.master.application.dto.UpdateSupplierDTO;
import com.jingwei.master.application.service.SupplierApplicationService;
import com.jingwei.master.interfaces.vo.SupplierVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 供应商管理 Controller
 * <p>
 * 提供供应商档案的 CRUD 接口。
 * 所有接口统一使用 POST 方法。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierApplicationService supplierApplicationService;

    /**
     * 创建供应商
     * <p>
     * 编码由编码规则引擎自动生成（格式：SUP-000001），
     * 状态默认 ACTIVE，资质默认 PENDING。
     * </p>
     */
    @RequirePermission("master:supplier:create")
    @PostMapping("/master/supplier/create")
    public R<SupplierVO> createSupplier(@Valid @RequestBody CreateSupplierDTO dto) {
        return R.ok(supplierApplicationService.createSupplier(dto));
    }

    /**
     * 更新供应商
     * <p>
     * 可更新字段：name, shortName, contactPerson, contactPhone, address,
     * settlementType, leadTimeDays, qualificationStatus, remark。
     * 编码和类型不可修改。
     * </p>
     */
    @RequirePermission("master:supplier:update")
    @PostMapping("/master/supplier/update")
    public R<SupplierVO> updateSupplier(@RequestParam Long supplierId,
                                        @Valid @RequestBody UpdateSupplierDTO dto) {
        return R.ok(supplierApplicationService.updateSupplier(supplierId, dto));
    }

    /**
     * 停用供应商
     * <p>
     * 停用后不可创建新采购订单，已有采购订单不受影响。
     * </p>
     */
    @RequirePermission("master:supplier:deactivate")
    @PostMapping("/master/supplier/deactivate")
    public R<Void> deactivateSupplier(@RequestParam Long supplierId) {
        supplierApplicationService.deactivateSupplier(supplierId);
        return R.ok();
    }

    /**
     * 启用供应商
     * <p>
     * 将停用的供应商重新启用，启用后可创建新采购订单。
     * </p>
     */
    @RequirePermission("master:supplier:activate")
    @PostMapping("/master/supplier/activate")
    public R<Void> activateSupplier(@RequestParam Long supplierId) {
        supplierApplicationService.activateSupplier(supplierId);
        return R.ok();
    }

    /**
     * 删除供应商
     * <p>
     * 仅允许删除未被采购订单引用的供应商。
     * </p>
     */
    @RequirePermission("master:supplier:delete")
    @PostMapping("/master/supplier/delete")
    public R<Void> deleteSupplier(@RequestParam Long supplierId) {
        supplierApplicationService.deleteSupplier(supplierId);
        return R.ok();
    }

    /**
     * 查询供应商详情
     */
    @PostMapping("/master/supplier/detail")
    public R<SupplierVO> getSupplierDetail(@RequestParam Long supplierId) {
        return R.ok(supplierApplicationService.getSupplierById(supplierId));
    }

    /**
     * 分页查询供应商
     * <p>
     * 支持按类型、资质状态、状态筛选，支持按编码或名称关键词搜索。
     * </p>
     */
    @PostMapping("/master/supplier/page")
    public R<IPage<SupplierVO>> pageQuery(@Valid @RequestBody SupplierQueryDTO dto) {
        return R.ok(supplierApplicationService.pageQuery(dto));
    }
}
