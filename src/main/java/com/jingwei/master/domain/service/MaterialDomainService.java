package com.jingwei.master.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.CommonStatus;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.*;
import com.jingwei.master.domain.repository.AttributeDefRepository;
import com.jingwei.master.domain.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 物料主数据领域服务
 * <p>
 * 核心职责：
 * <ul>
 *   <li>物料 CRUD 及业务校验</li>
 *   <li>JSONB 扩展属性的必填项校验（基于属性定义元数据驱动）</li>
 *   <li>成分百分比合计校验（COMPOSITION 类型属性，纤维百分比之和必须等于 100%）</li>
 *   <li>物料编码由编码规则引擎自动生成（调用方传入，本服务校验不可手动指定）</li>
 * </ul>
 * </p>
 * <p>
 * 设计决策：
 * <ul>
 *   <li>物料类型枚举仅包含 FABRIC/TRIM/PACKAGING，不含 PRODUCT——成品走 SPU/SKU 模型</li>
 *   <li>ext_attrs 使用 Map&lt;String, Object&gt; 而非具体 POJO——前端可直接透传，后端按属性定义校验</li>
 *   <li>成分校验是面料/辅料的特殊规则，仅在 ext_attrs 中存在 "composition" 键时触发</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialDomainService {

    private final MaterialRepository materialRepository;
    private final AttributeDefRepository attributeDefRepository;

    /**
     * 获取物料仓库引用（供 ApplicationService 分页查询使用）
     */
    public MaterialRepository getMaterialRepository() {
        return materialRepository;
    }

    /**
     * 创建物料
     * <p>
     * 校验规则：
     * <ol>
     *   <li>物料编码由编码规则引擎生成，不可为空</li>
     *   <li>物料编码不可重复（应用层校验 + 数据库唯一索引兜底）</li>
     *   <li>ext_attrs 必填属性校验：根据属性定义表检查必填项是否已填写</li>
     *   <li>成分百分比合计必须等于 100%</li>
     * </ol>
     * </p>
     *
     * @param material 物料实体（code 应由调用方从编码规则引擎获取后设置）
     * @return 保存后的物料实体
     */
    public Material createMaterial(Material material) {
        // 编码不可为空（应由编码规则引擎生成）
        if (material.getCode() == null || material.getCode().isBlank()) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "物料编码不能为空");
        }

        // 编码唯一性校验
        if (materialRepository.existsByCode(material.getCode())) {
            throw new BizException(ErrorCode.MATERIAL_CODE_DUPLICATE);
        }

        // 扩展属性必填项校验
        validateRequiredAttributes(material.getType(), material.getExtAttrs());

        // 成分百分比合计校验
        validateComposition(material.getExtAttrs());

        material.setStatus(CommonStatus.ACTIVE);
        try {
            materialRepository.insert(material);
        } catch (DuplicateKeyException e) {
            log.warn("并发创建物料触发唯一约束: code={}", material.getCode());
            throw new BizException(ErrorCode.MATERIAL_CODE_DUPLICATE);
        }

        log.info("创建物料: code={}, name={}, type={}, id={}",
                material.getCode(), material.getName(), material.getType(), material.getId());
        return material;
    }

    /**
     * 更新物料
     * <p>
     * 可更新字段：name, categoryId, unit, extAttrs, remark, status。
     * 物料编码和类型不可修改。
     * </p>
     *
     * @param materialId 物料ID
     * @param material   包含更新字段的物料实体
     * @return 更新后的物料
     */
    public Material updateMaterial(Long materialId, Material material) {
        Material existing = materialRepository.selectById(materialId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "物料不存在");
        }

        // 编码和类型不可修改
        material.setId(materialId);
        material.setCode(existing.getCode());
        material.setType(existing.getType());

        // 如果更新了 extAttrs，需要校验必填项和成分
        if (material.getExtAttrs() != null) {
            validateRequiredAttributes(existing.getType(), material.getExtAttrs());
            validateComposition(material.getExtAttrs());
        }

        int rows = materialRepository.updateById(material);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("更新物料: id={}", materialId);
        return materialRepository.selectById(materialId);
    }

    /**
     * 停用物料
     * <p>
     * 停用后不可在业务单据中选择，已有引用不受影响。
     * </p>
     *
     * @param materialId 物料ID
     */
    public void deactivateMaterial(Long materialId) {
        Material existing = materialRepository.selectById(materialId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "物料不存在");
        }

        existing.setStatus(CommonStatus.INACTIVE);
        int rows = materialRepository.updateById(existing);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        log.info("停用物料: id={}, code={}", materialId, existing.getCode());
    }

    /**
     * 根据ID查询物料详情
     *
     * @param materialId 物料ID
     * @return 物料实体
     */
    public Material getMaterialById(Long materialId) {
        Material material = materialRepository.selectById(materialId);
        if (material == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "物料不存在");
        }
        return material;
    }

    // ==================== 属性校验 ====================

    /**
     * 校验必填属性
     * <p>
     * 根据属性定义表中该物料类型的 required=true 的属性定义，
     * 检查 ext_attrs 中对应的 ext_json_path 是否有值。
     * </p>
     *
     * @param type      物料类型
     * @param extAttrs  扩展属性 Map
     */
    void validateRequiredAttributes(MaterialType type, Map<String, Object> extAttrs) {
        List<AttributeDef> defs = attributeDefRepository.selectByMaterialType(type);
        List<String> missingFields = new ArrayList<>();

        for (AttributeDef def : defs) {
            if (Boolean.TRUE.equals(def.getRequired())) {
                Object value = extAttrs != null ? extAttrs.get(def.getExtJsonPath()) : null;
                // 值为 null 或空字符串视为未填写
                if (value == null || (value instanceof String s && s.isBlank())) {
                    missingFields.add(def.getName());
                }
            }
        }

        if (!missingFields.isEmpty()) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "缺少必填属性：" + String.join("、", missingFields));
        }
    }

    /**
     * 校验成分百分比合计
     * <p>
     * 仅当 ext_attrs 中存在 "composition" 键时触发校验。
     * composition 结构为：[{"fiber":"棉","percentage":80},{"fiber":"涤纶","percentage":20}]
     * 所有纤维的 percentage 之和必须等于 100。
     * </p>
     * <p>
     * 为什么在这里校验而不是在属性定义层：成分百分比是跨属性的组合校验规则，
     * 不属于单个属性的校验范围，是面料/辅料特有的业务规则。
     * </p>
     *
     * @param extAttrs 扩展属性 Map
     */
    void validateComposition(Map<String, Object> extAttrs) {
        if (extAttrs == null || !extAttrs.containsKey("composition")) {
            return;
        }

        Object compositionObj = extAttrs.get("composition");
        if (!(compositionObj instanceof List<?> compositionList)) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "成分数据格式不正确");
        }

        double total = 0;
        for (Object item : compositionList) {
            if (item instanceof Map<?, ?> entry) {
                Object pct = entry.get("percentage");
                if (pct instanceof Number n) {
                    total += n.doubleValue();
                }
            }
        }

        // 使用 0.01 容差避免浮点精度问题
        if (Math.abs(total - 100.0) > 0.01) {
            throw new BizException(ErrorCode.COMPOSITION_PERCENT_INVALID);
        }
    }
}
