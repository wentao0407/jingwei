package com.jingwei.procurement.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.master.domain.model.Material;
import com.jingwei.master.domain.model.Supplier;
import com.jingwei.master.domain.repository.MaterialRepository;
import com.jingwei.master.domain.repository.SupplierRepository;
import com.jingwei.procurement.application.dto.MrpCalculateDTO;
import com.jingwei.procurement.application.dto.MrpQueryDTO;
import com.jingwei.procurement.domain.model.MrpCalculateResult;
import com.jingwei.procurement.domain.model.MrpResult;
import com.jingwei.procurement.domain.model.MrpResultStatus;
import com.jingwei.procurement.domain.repository.MrpResultRepository;
import com.jingwei.procurement.domain.service.MrpEngine;
import com.jingwei.procurement.interfaces.vo.MrpCalculateResultVO;
import com.jingwei.procurement.interfaces.vo.MrpResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * MRP 应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MrpApplicationService {

    private final MrpEngine mrpEngine;
    private final MrpResultRepository mrpResultRepository;
    private final MaterialRepository materialRepository;
    private final SupplierRepository supplierRepository;

    /**
     * 执行 MRP 计算
     */
    @Transactional(rollbackFor = Exception.class)
    public MrpCalculateResultVO calculate(MrpCalculateDTO dto) {
        MrpCalculateResult calcResult = mrpEngine.calculate(dto.getProductionOrderIds());

        MrpCalculateResultVO vo = new MrpCalculateResultVO();
        vo.setResults(calcResult.getResults().stream().map(this::toMrpResultVO).toList());
        vo.setTotalItems(calcResult.getResults().size());
        vo.setWarnings(calcResult.getWarnings());
        return vo;
    }

    /**
     * 分页查询 MRP 结果
     */
    public IPage<MrpResultVO> pageQuery(MrpQueryDTO dto) {
        Page<MrpResult> page = new Page<>(dto.getCurrent(), dto.getSize());
        MrpResultStatus status = dto.getStatus() != null ? MrpResultStatus.valueOf(dto.getStatus()) : null;

        IPage<MrpResult> resultPage = mrpResultRepository.selectPage(page, dto.getBatchNo(), status);
        return resultPage.convert(this::toMrpResultVO);
    }

    private MrpResultVO toMrpResultVO(MrpResult result) {
        MrpResultVO vo = new MrpResultVO();
        vo.setId(result.getId());
        vo.setBatchNo(result.getBatchNo());
        vo.setMaterialId(result.getMaterialId());
        vo.setMaterialType(result.getMaterialType());
        vo.setGrossDemand(result.getGrossDemand());
        vo.setAllocatedStock(result.getAllocatedStock());
        vo.setInTransitQuantity(result.getInTransitQuantity());
        vo.setNetDemand(result.getNetDemand());
        vo.setSuggestedQuantity(result.getSuggestedQuantity());
        vo.setUnit(result.getUnit());
        vo.setSuggestedSupplierId(result.getSuggestedSupplierId());
        vo.setEstimatedCost(result.getEstimatedCost());
        vo.setStatus(result.getStatus() != null ? result.getStatus().name() : null);
        vo.setStatusLabel(result.getStatus() != null ? result.getStatus().getLabel() : null);
        vo.setSnapshotTime(result.getSnapshotTime());
        vo.setRemark(result.getRemark());

        // 补充物料展示信息
        if (result.getMaterialId() != null) {
            Material material = materialRepository.selectById(result.getMaterialId());
            if (material != null) {
                vo.setMaterialCode(material.getCode());
                vo.setMaterialName(material.getName());
            }
        }

        // 补充供应商展示信息
        if (result.getSuggestedSupplierId() != null) {
            Supplier supplier = supplierRepository.selectById(result.getSuggestedSupplierId());
            if (supplier != null) {
                vo.setSuggestedSupplierName(supplier.getName());
            }
        }

        return vo;
    }
}
