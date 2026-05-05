package com.jingwei.master.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.master.application.dto.CreateSupplierDTO;
import com.jingwei.master.application.dto.SupplierQueryDTO;
import com.jingwei.master.application.dto.UpdateSupplierDTO;
import com.jingwei.master.domain.model.*;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.master.domain.service.SupplierDomainService;
import com.jingwei.master.interfaces.vo.SupplierVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 供应商应用服务
 * <p>
 * 负责供应商 CRUD 的编排和事务边界管理。
 * 供应商编码生成调用编码规则引擎（CodingRuleDomainService），
 * 业务校验委托给 SupplierDomainService。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierApplicationService {

    /** 编码规则键：供应商编码（格式 SUP-6位流水号），与 t_md_coding_rule 的 rule_code 对应 */
    private static final String SUPPLIER_CODE_RULE = "SUPPLIER_CODE";

    private final SupplierDomainService supplierDomainService;
    private final CodingRuleDomainService codingRuleDomainService;

    /**
     * 创建供应商
     * <p>
     * 编排流程：
     * <ol>
     *   <li>调用编码规则引擎生成供应商编码</li>
     *   <li>组装 Supplier 实体</li>
     *   <li>调用 DomainService 执行业务校验和持久化</li>
     * </ol>
     * </p>
     *
     * @param dto 创建请求
     * @return 供应商 VO
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierVO createSupplier(CreateSupplierDTO dto) {
        // 1. 调用编码规则引擎生成供应商编码
        String code = codingRuleDomainService.generateCode(SUPPLIER_CODE_RULE, Map.of());

        // 2. 组装实体
        Supplier supplier = new Supplier();
        supplier.setCode(code);
        supplier.setName(dto.getName());
        supplier.setShortName(dto.getShortName());
        supplier.setType(SupplierType.valueOf(dto.getType()));
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setContactPhone(dto.getContactPhone());
        supplier.setAddress(dto.getAddress());
        if (dto.getSettlementType() != null) {
            supplier.setSettlementType(SettlementType.valueOf(dto.getSettlementType()));
        }
        supplier.setLeadTimeDays(dto.getLeadTimeDays());
        supplier.setRemark(dto.getRemark() != null ? dto.getRemark() : "");

        // 3. 业务校验和持久化
        Supplier saved = supplierDomainService.createSupplier(supplier);
        return toSupplierVO(saved);
    }

    /**
     * 更新供应商
     * <p>
     * 可更新字段：name, shortName, contactPerson, contactPhone, address,
     * settlementType, leadTimeDays, qualificationStatus, remark。
     * 编码和类型不可修改。
     * </p>
     *
     * @param supplierId 供应商ID
     * @param dto        更新请求
     * @return 供应商 VO
     */
    @Transactional(rollbackFor = Exception.class)
    public SupplierVO updateSupplier(Long supplierId, UpdateSupplierDTO dto) {
        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setShortName(dto.getShortName());
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setContactPhone(dto.getContactPhone());
        supplier.setAddress(dto.getAddress());
        if (dto.getSettlementType() != null) {
            supplier.setSettlementType(SettlementType.valueOf(dto.getSettlementType()));
        }
        supplier.setLeadTimeDays(dto.getLeadTimeDays());
        if (dto.getQualificationStatus() != null) {
            supplier.setQualificationStatus(SupplierQualificationStatus.valueOf(dto.getQualificationStatus()));
        }
        supplier.setRemark(dto.getRemark());

        Supplier updated = supplierDomainService.updateSupplier(supplierId, supplier);
        return toSupplierVO(updated);
    }

    /**
     * 停用供应商
     * <p>
     * 停用后不可创建新采购订单，已有采购订单不受影响。
     * </p>
     *
     * @param supplierId 供应商ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deactivateSupplier(Long supplierId) {
        supplierDomainService.deactivateSupplier(supplierId);
    }

    /**
     * 启用供应商
     * <p>
     * 将停用的供应商重新启用，启用后可创建新采购订单。
     * </p>
     *
     * @param supplierId 供应商ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void activateSupplier(Long supplierId) {
        supplierDomainService.activateSupplier(supplierId);
    }

    /**
     * 删除供应商
     * <p>
     * 仅允许删除未被采购订单引用的供应商。
     * </p>
     *
     * @param supplierId 供应商ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSupplier(Long supplierId) {
        supplierDomainService.deleteSupplier(supplierId);
    }

    /**
     * 根据ID查询供应商详情
     *
     * @param supplierId 供应商ID
     * @return 供应商 VO
     */
    public SupplierVO getSupplierById(Long supplierId) {
        Supplier supplier = supplierDomainService.getSupplierById(supplierId);
        return toSupplierVO(supplier);
    }

    /**
     * 分页查询供应商
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    public IPage<SupplierVO> pageQuery(SupplierQueryDTO dto) {
        Page<Supplier> page = new Page<>(dto.getCurrent(), dto.getSize());

        IPage<Supplier> supplierPage = supplierDomainService.getSupplierRepository()
                .selectPage(page, dto.getType(), dto.getQualificationStatus(),
                        dto.getStatus(), dto.getKeyword());

        return supplierPage.convert(this::toSupplierVO);
    }

    // ==================== 转换方法 ====================

    /**
     * 将 Supplier 实体转换为 SupplierVO
     *
     * @param supplier 供应商实体
     * @return 供应商 VO
     */
    private SupplierVO toSupplierVO(Supplier supplier) {
        SupplierVO vo = new SupplierVO();
        vo.setId(supplier.getId());
        vo.setCode(supplier.getCode());
        vo.setName(supplier.getName());
        vo.setShortName(supplier.getShortName());
        vo.setType(supplier.getType() != null ? supplier.getType().name() : null);
        vo.setContactPerson(supplier.getContactPerson());
        vo.setContactPhone(supplier.getContactPhone());
        vo.setAddress(supplier.getAddress());
        vo.setSettlementType(supplier.getSettlementType() != null ? supplier.getSettlementType().name() : null);
        vo.setLeadTimeDays(supplier.getLeadTimeDays());
        vo.setQualificationStatus(supplier.getQualificationStatus() != null
                ? supplier.getQualificationStatus().name() : null);
        vo.setStatus(supplier.getStatus() != null ? supplier.getStatus().name() : null);
        vo.setRemark(supplier.getRemark());
        vo.setCreatedAt(supplier.getCreatedAt());
        vo.setUpdatedAt(supplier.getUpdatedAt());
        return vo;
    }
}
