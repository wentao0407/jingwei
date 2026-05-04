package com.jingwei.procurement.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.UserContext;
import com.jingwei.master.domain.model.Material;
import com.jingwei.master.domain.model.Spu;
import com.jingwei.master.domain.repository.MaterialRepository;
import com.jingwei.master.domain.repository.SpuRepository;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.procurement.application.dto.BomItemCreateDTO;
import com.jingwei.procurement.application.dto.BomQueryDTO;
import com.jingwei.procurement.application.dto.CreateBomDTO;
import com.jingwei.procurement.application.dto.UpdateBomDTO;
import com.jingwei.procurement.domain.model.Bom;
import com.jingwei.procurement.domain.model.BomItem;
import com.jingwei.procurement.domain.model.BomStatus;
import com.jingwei.procurement.domain.model.ConsumptionType;
import com.jingwei.procurement.domain.model.SizeConsumptions;
import com.jingwei.procurement.domain.service.BomDomainService;
import com.jingwei.procurement.domain.repository.BomRepository;
import com.jingwei.procurement.interfaces.vo.BomItemVO;
import com.jingwei.procurement.interfaces.vo.BomVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * BOM 应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BomApplicationService {

    private static final String BOM_CODE_RULE = "BOM";

    private final BomDomainService bomDomainService;
    private final BomRepository bomRepository;
    private final CodingRuleDomainService codingRuleDomainService;
    private final SpuRepository spuRepository;
    private final MaterialRepository materialRepository;

    /**
     * 创建 BOM
     */
    @Transactional(rollbackFor = Exception.class)
    public BomVO createBom(CreateBomDTO dto) {
        String bomCode = codingRuleDomainService.generateCode(
                BOM_CODE_RULE, Collections.emptyMap());

        Bom bom = new Bom();
        bom.setCode(bomCode);
        bom.setSpuId(dto.getSpuId());
        bom.setEffectiveFrom(dto.getEffectiveFrom() != null ? LocalDate.parse(dto.getEffectiveFrom()) : null);
        bom.setEffectiveTo(dto.getEffectiveTo() != null ? LocalDate.parse(dto.getEffectiveTo()) : null);
        bom.setRemark(dto.getRemark());

        List<BomItem> items = buildBomItems(dto.getItems());

        Bom saved = bomDomainService.createBom(bom, items);
        return toBomVO(saved);
    }

    /**
     * 编辑 BOM
     */
    @Transactional(rollbackFor = Exception.class)
    public BomVO updateBom(Long bomId, UpdateBomDTO dto) {
        Long operatorId = UserContext.getUserId();

        Bom updatedBom = new Bom();
        updatedBom.setEffectiveFrom(dto.getEffectiveFrom() != null ? LocalDate.parse(dto.getEffectiveFrom()) : null);
        updatedBom.setEffectiveTo(dto.getEffectiveTo() != null ? LocalDate.parse(dto.getEffectiveTo()) : null);
        updatedBom.setRemark(dto.getRemark());

        List<BomItem> items = buildBomItems(dto.getItems());

        Bom saved = bomDomainService.updateBom(bomId, updatedBom, items, operatorId);
        return toBomVO(saved);
    }

    /**
     * 删除 BOM
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBom(Long bomId) {
        bomDomainService.deleteBom(bomId);
    }

    /**
     * 审批 BOM
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveBom(Long bomId) {
        Long approverId = UserContext.getUserId();
        bomDomainService.approveBom(bomId, approverId);
    }

    /**
     * 查询 BOM 详情
     */
    public BomVO getDetail(Long bomId) {
        Bom bom = bomDomainService.getBomDetail(bomId);
        return toBomVO(bom);
    }

    /**
     * 分页查询 BOM
     */
    public IPage<BomVO> pageQuery(BomQueryDTO dto) {
        Page<Bom> page = new Page<>(dto.getCurrent(), dto.getSize());
        BomStatus status = dto.getStatus() != null ? BomStatus.valueOf(dto.getStatus()) : null;

        IPage<Bom> bomPage = bomRepository.selectPage(page, dto.getSpuId(), status);
        return bomPage.convert(this::toBomVO);
    }

    // ==================== 私有方法 ====================

    private List<BomItem> buildBomItems(List<BomItemCreateDTO> itemDTOs) {
        List<BomItem> items = new ArrayList<>();
        for (BomItemCreateDTO dto : itemDTOs) {
            BomItem item = new BomItem();
            item.setMaterialId(dto.getMaterialId());
            item.setMaterialType(dto.getMaterialType());
            item.setConsumptionType(ConsumptionType.valueOf(dto.getConsumptionType()));
            item.setBaseConsumption(dto.getBaseConsumption());
            item.setBaseSizeId(dto.getBaseSizeId());
            item.setUnit(dto.getUnit());
            item.setWastageRate(dto.getWastageRate() != null ? dto.getWastageRate() : BigDecimal.ZERO);
            item.setRemark(dto.getRemark());

            // 构建尺码用量表
            if (dto.getSizeConsumptions() != null && !dto.getSizeConsumptions().isEmpty()) {
                List<SizeConsumptions.SizeConsumptionEntry> entries = dto.getSizeConsumptions().stream()
                        .map(s -> new SizeConsumptions.SizeConsumptionEntry(s.getSizeId(), s.getCode(), s.getConsumption()))
                        .toList();
                SizeConsumptions sc = new SizeConsumptions(
                        dto.getBaseSizeId(), null, dto.getBaseConsumption(), entries);
                item.setSizeConsumptions(sc);
            }

            items.add(item);
        }
        return items;
    }

    private BomVO toBomVO(Bom bom) {
        BomVO vo = new BomVO();
        vo.setId(bom.getId());
        vo.setCode(bom.getCode());
        vo.setSpuId(bom.getSpuId());
        vo.setBomVersion(bom.getBomVersion());
        vo.setStatus(bom.getStatus() != null ? bom.getStatus().name() : null);
        vo.setStatusLabel(bom.getStatus() != null ? bom.getStatus().getLabel() : null);
        vo.setEffectiveFrom(bom.getEffectiveFrom() != null ? bom.getEffectiveFrom().toString() : null);
        vo.setEffectiveTo(bom.getEffectiveTo() != null ? bom.getEffectiveTo().toString() : null);
        vo.setApprovedBy(bom.getApprovedBy());
        vo.setApprovedAt(bom.getApprovedAt());
        vo.setRemark(bom.getRemark());
        vo.setCreatedAt(bom.getCreatedAt());
        vo.setUpdatedAt(bom.getUpdatedAt());

        // 补充款式展示信息
        if (bom.getSpuId() != null) {
            Spu spu = spuRepository.selectById(bom.getSpuId());
            if (spu != null) {
                vo.setSpuCode(spu.getCode());
                vo.setSpuName(spu.getName());
            }
        }

        // 转换行
        if (bom.getItems() != null && !bom.getItems().isEmpty()) {
            vo.setItems(bom.getItems().stream().map(this::toBomItemVO).toList());
        } else {
            vo.setItems(List.of());
        }

        return vo;
    }

    private BomItemVO toBomItemVO(BomItem item) {
        BomItemVO vo = new BomItemVO();
        vo.setId(item.getId());
        vo.setBomId(item.getBomId());
        vo.setMaterialId(item.getMaterialId());
        vo.setMaterialType(item.getMaterialType());
        vo.setConsumptionType(item.getConsumptionType() != null ? item.getConsumptionType().name() : null);
        vo.setConsumptionTypeLabel(item.getConsumptionType() != null ? item.getConsumptionType().getLabel() : null);
        vo.setBaseConsumption(item.getBaseConsumption());
        vo.setBaseSizeId(item.getBaseSizeId());
        vo.setUnit(item.getUnit());
        vo.setWastageRate(item.getWastageRate());
        vo.setSortOrder(item.getSortOrder());
        vo.setRemark(item.getRemark());

        // 补充物料展示信息
        if (item.getMaterialId() != null) {
            Material material = materialRepository.selectById(item.getMaterialId());
            if (material != null) {
                vo.setMaterialCode(material.getCode());
                vo.setMaterialName(material.getName());
            }
        }

        // 转换尺码用量表
        if (item.getSizeConsumptions() != null) {
            vo.setSizeConsumptions(Map.of(
                    "baseSizeId", item.getSizeConsumptions().getBaseSizeId(),
                    "baseConsumption", item.getSizeConsumptions().getBaseConsumption(),
                    "sizes", item.getSizeConsumptions().getSizes()
            ));
        }

        return vo;
    }
}
