package com.jingwei.master.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.CommonStatus;
import com.jingwei.master.application.dto.CreateMaterialDTO;
import com.jingwei.master.application.dto.MaterialQueryDTO;
import com.jingwei.master.application.dto.UpdateMaterialDTO;
import com.jingwei.master.domain.model.*;
import com.jingwei.master.domain.repository.AttributeDefRepository;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.master.domain.service.MaterialDomainService;
import com.jingwei.master.interfaces.vo.AttributeDefVO;
import com.jingwei.master.interfaces.vo.MaterialVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 物料主数据应用服务
 * <p>
 * 负责物料 CRUD 的编排和事务边界管理。
 * 物料编码生成调用编码规则引擎（CodingRuleDomainService），
 * 业务校验委托给 MaterialDomainService。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialApplicationService {

    private static final String MATERIAL_CODE_RULE = "MATERIAL_CODE";

    private final MaterialDomainService materialDomainService;
    private final CodingRuleDomainService codingRuleDomainService;
    private final AttributeDefRepository attributeDefRepository;

    /**
     * 创建物料
     * <p>
     * 编排流程：
     * <ol>
     *   <li>调用编码规则引擎生成物料编码</li>
     *   <li>组装 Material 实体</li>
     *   <li>调用 DomainService 执行业务校验和持久化</li>
     * </ol>
     * </p>
     */
    @Transactional(rollbackFor = Exception.class)
    public MaterialVO createMaterial(CreateMaterialDTO dto) {
        // 1. 调用编码规则引擎生成物料编码
        String code = codingRuleDomainService.generateCode(MATERIAL_CODE_RULE, Map.of());

        // 2. 组装实体
        Material material = new Material();
        material.setCode(code);
        material.setName(dto.getName());
        material.setType(MaterialType.valueOf(dto.getType()));
        material.setCategoryId(dto.getCategoryId());
        material.setUnit(dto.getUnit());
        material.setExtAttrs(dto.getExtAttrs());
        material.setRemark(dto.getRemark() != null ? dto.getRemark() : "");

        // 3. 业务校验和持久化
        Material saved = materialDomainService.createMaterial(material);
        return toMaterialVO(saved);
    }

    /**
     * 更新物料
     */
    @Transactional(rollbackFor = Exception.class)
    public MaterialVO updateMaterial(Long materialId, UpdateMaterialDTO dto) {
        Material material = new Material();
        material.setName(dto.getName());
        material.setCategoryId(dto.getCategoryId());
        material.setUnit(dto.getUnit());
        material.setExtAttrs(dto.getExtAttrs());
        material.setRemark(dto.getRemark());
        if (dto.getStatus() != null) {
            material.setStatus(CommonStatus.valueOf(dto.getStatus()));
        }

        Material updated = materialDomainService.updateMaterial(materialId, material);
        return toMaterialVO(updated);
    }

    /**
     * 停用物料
     */
    @Transactional(rollbackFor = Exception.class)
    public void deactivateMaterial(Long materialId) {
        materialDomainService.deactivateMaterial(materialId);
    }

    /**
     * 根据ID查询物料详情
     */
    public MaterialVO getMaterialById(Long materialId) {
        Material material = materialDomainService.getMaterialById(materialId);
        return toMaterialVO(material);
    }

    /**
     * 分页查询物料
     */
    public IPage<MaterialVO> pageQuery(MaterialQueryDTO dto) {
        Page<Material> page = new Page<>(dto.getCurrent(), dto.getSize());
        MaterialType type = dto.getType() != null ? MaterialType.valueOf(dto.getType()) : null;

        IPage<Material> materialPage = materialDomainService.getMaterialRepository()
                .selectPage(page, type, dto.getCategoryId(), dto.getStatus(), dto.getKeyword());

        return materialPage.convert(this::toMaterialVO);
    }

    /**
     * 查询指定物料类型的属性定义
     * <p>
     * 前端根据此接口渲染动态属性表单。
     * </p>
     */
    public List<AttributeDefVO> getAttributeDefs(String materialType) {
        MaterialType type = MaterialType.valueOf(materialType);
        List<AttributeDef> defs = attributeDefRepository.selectByMaterialType(type);
        return defs.stream().map(this::toAttributeDefVO).toList();
    }

    // ==================== 转换方法 ====================

    private MaterialVO toMaterialVO(Material material) {
        MaterialVO vo = new MaterialVO();
        vo.setId(material.getId());
        vo.setCode(material.getCode());
        vo.setName(material.getName());
        vo.setType(material.getType().name());
        vo.setCategoryId(material.getCategoryId());
        vo.setUnit(material.getUnit());
        vo.setStatus(material.getStatus().name());
        vo.setExtAttrs(material.getExtAttrs());
        vo.setRemark(material.getRemark());
        vo.setCreatedAt(material.getCreatedAt());
        vo.setUpdatedAt(material.getUpdatedAt());
        return vo;
    }

    private AttributeDefVO toAttributeDefVO(AttributeDef def) {
        AttributeDefVO vo = new AttributeDefVO();
        vo.setId(def.getId());
        vo.setCode(def.getCode());
        vo.setName(def.getName());
        vo.setMaterialType(def.getMaterialType().name());
        vo.setInputType(def.getInputType().name());
        vo.setRequired(def.getRequired());
        vo.setSortOrder(def.getSortOrder());
        vo.setOptions(def.getOptions());
        vo.setExtJsonPath(def.getExtJsonPath());
        return vo;
    }
}
