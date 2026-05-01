package com.jingwei.master.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.CommonStatus;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.Supplier;
import com.jingwei.master.domain.model.SupplierQualificationStatus;
import com.jingwei.master.domain.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 供应商领域服务
 * <p>
 * 核心职责：
 * <ul>
 *   <li>供应商 CRUD 及业务校验</li>
 *   <li>名称唯一性校验（全局唯一，不允许同名供应商）</li>
 *   <li>供应商停用控制（停用后不可创建新采购订单，已有采购订单不受影响）</li>
 *   <li>资质状态管理（不合格供应商不可在采购订单中选择）</li>
 * </ul>
 * </p>
 * <p>
 * 关键业务规则：
 * <ul>
 *   <li>供应商编码由编码规则引擎自动生成，不可手动指定</li>
 *   <li>供应商名称全局唯一——同名供应商可能是不同分支机构，但服装行业以名称为主标识</li>
 *   <li>不合格（DISQUALIFIED）供应商不可在采购订单中选择——保障采购质量</li>
 *   <li>停用（INACTIVE）供应商不可创建新采购订单——停用是供应商关系的终止信号</li>
 *   <li>供应商类型不可修改——类型影响采购策略和 BOM 关联，变更会导致历史数据不一致</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierDomainService {

    private final SupplierRepository supplierRepository;

    /**
     * 获取供应商仓库引用（供 ApplicationService 分页查询使用）
     *
     * @return 供应商仓库
     */
    public SupplierRepository getSupplierRepository() {
        return supplierRepository;
    }

    /**
     * 创建供应商
     * <p>
     * 校验规则：
     * <ol>
     *   <li>供应商编码由编码规则引擎生成，不可为空</li>
     *   <li>供应商编码不可重复（应用层校验 + 数据库唯一索引兜底）</li>
     *   <li>供应商名称全局唯一</li>
     * </ol>
     * </p>
     *
     * @param supplier 供应商实体（code 应由调用方从编码规则引擎获取后设置）
     * @return 保存后的供应商实体
     */
    public Supplier createSupplier(Supplier supplier) {
        // 编码不可为空（应由编码规则引擎生成）
        if (supplier.getCode() == null || supplier.getCode().isBlank()) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "供应商编码不能为空");
        }

        // 编码唯一性校验
        if (supplierRepository.existsByCode(supplier.getCode())) {
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "供应商编码已存在");
        }

        // 名称唯一性校验
        if (supplierRepository.existsByName(supplier.getName(), null)) {
            throw new BizException(ErrorCode.SUPPLIER_NAME_DUPLICATE);
        }

        // 新建供应商默认为 ACTIVE 状态、待审资质
        supplier.setStatus(CommonStatus.ACTIVE);
        if (supplier.getQualificationStatus() == null) {
            supplier.setQualificationStatus(SupplierQualificationStatus.PENDING);
        }

        try {
            supplierRepository.insert(supplier);
        } catch (DuplicateKeyException e) {
            log.warn("并发创建供应商触发唯一约束: code={}", supplier.getCode());
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "供应商编码或名称已存在");
        }

        log.info("创建供应商: code={}, name={}, type={}, id={}",
                supplier.getCode(), supplier.getName(), supplier.getType(), supplier.getId());
        return supplier;
    }

    /**
     * 更新供应商
     * <p>
     * 可更新字段：name, shortName, contactPerson, contactPhone, address,
     * settlementType, leadTimeDays, qualificationStatus, remark。
     * 供应商编码（code）和类型（type）不可修改——编码被采购订单等业务单据引用，
     * 类型影响采购策略和 BOM 关联，变更会导致历史数据不一致。
     * </p>
     *
     * @param supplierId 供应商ID
     * @param supplier   包含更新字段的供应商实体
     * @return 更新后的供应商
     */
    public Supplier updateSupplier(Long supplierId, Supplier supplier) {
        Supplier existing = supplierRepository.selectById(supplierId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "供应商不存在");
        }

        // 名称唯一性校验（排除自身）
        if (supplier.getName() != null && !supplier.getName().equals(existing.getName())) {
            if (supplierRepository.existsByName(supplier.getName(), supplierId)) {
                throw new BizException(ErrorCode.SUPPLIER_NAME_DUPLICATE);
            }
        }

        // 编码和类型不可修改
        supplier.setId(supplierId);
        supplier.setCode(existing.getCode());
        supplier.setType(existing.getType());

        int rows = supplierRepository.updateById(supplier);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新供应商: id={}", supplierId);
        return supplierRepository.selectById(supplierId);
    }

    /**
     * 停用供应商
     * <p>
     * 停用后不可创建新采购订单，已有采购订单不受影响。
     * 已停用的供应商不允许重复停用。
     * </p>
     *
     * @param supplierId 供应商ID
     */
    public void deactivateSupplier(Long supplierId) {
        Supplier existing = supplierRepository.selectById(supplierId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "供应商不存在");
        }

        if (existing.getStatus() == CommonStatus.INACTIVE) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "供应商已停用");
        }

        existing.setStatus(CommonStatus.INACTIVE);
        int rows = supplierRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("停用供应商: id={}, code={}", supplierId, existing.getCode());
    }

    /**
     * 启用供应商
     * <p>
     * 将停用的供应商重新启用，启用后可创建新采购订单。
     * 已启用的供应商不允许重复启用。
     * </p>
     *
     * @param supplierId 供应商ID
     */
    public void activateSupplier(Long supplierId) {
        Supplier existing = supplierRepository.selectById(supplierId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "供应商不存在");
        }

        if (existing.getStatus() == CommonStatus.ACTIVE) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "供应商已启用");
        }

        existing.setStatus(CommonStatus.ACTIVE);
        int rows = supplierRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("启用供应商: id={}, code={}", supplierId, existing.getCode());
    }

    /**
     * 删除供应商
     * <p>
     * 仅允许删除未被业务单据引用的供应商。
     * 当前采购模块尚未实现，预留引用检查钩子。
     * </p>
     *
     * @param supplierId 供应商ID
     */
    public void deleteSupplier(Long supplierId) {
        Supplier existing = supplierRepository.selectById(supplierId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "供应商不存在");
        }

        // 检查是否被采购订单引用（当前采购模块尚未实现，预留钩子）
        long procurementCount = countProcurementReferences(supplierId);
        if (procurementCount > 0) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "该供应商已被" + procurementCount + "个采购订单引用，不可删除");
        }

        supplierRepository.deleteById(supplierId);
        log.info("删除供应商: id={}, code={}", supplierId, existing.getCode());
    }

    /**
     * 根据ID查询供应商详情
     *
     * @param supplierId 供应商ID
     * @return 供应商实体
     */
    public Supplier getSupplierById(Long supplierId) {
        Supplier supplier = supplierRepository.selectById(supplierId);
        if (supplier == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "供应商不存在");
        }
        return supplier;
    }

    // ==================== 私有方法 ====================

    /**
     * 统计引用该供应商的采购订单数量
     * <p>
     * 当前采购模块尚未实现，返回 0。
     * 采购模块实现后，应替换为真实查询。
     * </p>
     *
     * @param supplierId 供应商ID
     * @return 引用该供应商的采购订单数量
     */
    private long countProcurementReferences(Long supplierId) {
        // TODO: 采购模块实现后，注入采购订单 Mapper 并查询真实引用数量
        return 0;
    }
}
