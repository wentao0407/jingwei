package com.jingwei.master;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.CommonStatus;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.model.*;
import com.jingwei.master.domain.repository.AttributeDefRepository;
import com.jingwei.master.domain.repository.MaterialRepository;
import com.jingwei.master.domain.service.MaterialDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MaterialDomainService 单元测试
 * <p>
 * 测试物料主数据领域服务的核心业务规则：
 * <ul>
 *   <li>创建物料：编码非空校验、编码唯一性、必填属性校验、成分百分比校验</li>
 *   <li>更新物料：编码和类型不可修改</li>
 *   <li>停用物料</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class MaterialDomainServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private AttributeDefRepository attributeDefRepository;

    @InjectMocks
    private MaterialDomainService materialDomainService;

    // ==================== 创建物料 ====================

    @Test
    @DisplayName("创建面料 — ext_attrs 正确存储克重、门幅、成分等")
    void createMaterial_fabric_shouldStoreExtAttrs() {
        Map<String, Object> extAttrs = Map.of(
                "weight", 280,
                "width", 150,
                "composition", List.of(
                        Map.of("fiber", "棉", "percentage", 80),
                        Map.of("fiber", "涤纶", "percentage", 20)
                ),
                "yarnCount", "40S/1"
        );

        when(materialRepository.existsByCode("ML-000001")).thenReturn(false);
        when(attributeDefRepository.selectByMaterialType(MaterialType.FABRIC))
                .thenReturn(buildFabricAttributeDefs());
        when(materialRepository.insert(any())).thenReturn(1);

        Material material = buildMaterial("ML-000001", "斜纹棉布", MaterialType.FABRIC, extAttrs);
        Material result = materialDomainService.createMaterial(material);

        assertNotNull(result);
        assertEquals(CommonStatus.ACTIVE, result.getStatus());
        assertEquals(MaterialType.FABRIC, result.getType());
        assertEquals(280, result.getExtAttrs().get("weight"));
        assertEquals(150, result.getExtAttrs().get("width"));
    }

    @Test
    @DisplayName("创建辅料 — ext_attrs 存储规格、材质、颜色等")
    void createMaterial_trim_shouldStoreExtAttrs() {
        Map<String, Object> extAttrs = Map.of(
                "spec", "4孔 18mm",
                "material", "树脂",
                "color", "黑色"
        );

        when(materialRepository.existsByCode("ML-000002")).thenReturn(false);
        when(attributeDefRepository.selectByMaterialType(MaterialType.TRIM))
                .thenReturn(buildTrimAttributeDefs());
        when(materialRepository.insert(any())).thenReturn(1);

        Material material = buildMaterial("ML-000002", "树脂纽扣", MaterialType.TRIM, extAttrs);
        Material result = materialDomainService.createMaterial(material);

        assertNotNull(result);
        assertEquals(MaterialType.TRIM, result.getType());
        assertEquals("4孔 18mm", result.getExtAttrs().get("spec"));
    }

    @Test
    @DisplayName("创建包材 — ext_attrs 存储规格、材质、厚度等")
    void createMaterial_packaging_shouldStoreExtAttrs() {
        Map<String, Object> extAttrs = Map.of(
                "spec", "60×40×30cm",
                "material", "瓦楞纸",
                "thickness", "5层"
        );

        when(materialRepository.existsByCode("ML-000003")).thenReturn(false);
        when(attributeDefRepository.selectByMaterialType(MaterialType.PACKAGING))
                .thenReturn(buildPackagingAttributeDefs());
        when(materialRepository.insert(any())).thenReturn(1);

        Material material = buildMaterial("ML-000003", "中号纸箱", MaterialType.PACKAGING, extAttrs);
        Material result = materialDomainService.createMaterial(material);

        assertNotNull(result);
        assertEquals(MaterialType.PACKAGING, result.getType());
        assertEquals("60×40×30cm", result.getExtAttrs().get("spec"));
    }

    @Test
    @DisplayName("创建物料 — 编码为空应抛异常")
    void createMaterial_codeBlank_shouldThrow() {
        Material material = new Material();
        material.setCode("");
        material.setName("测试");
        material.setType(MaterialType.FABRIC);

        BizException ex = assertThrows(BizException.class,
                () -> materialDomainService.createMaterial(material));
        assertTrue(ex.getMessage().contains("物料编码不能为空"));
    }

    @Test
    @DisplayName("创建物料 — 编码重复应抛异常")
    void createMaterial_duplicateCode_shouldThrow() {
        when(materialRepository.existsByCode("ML-000001")).thenReturn(true);

        Material material = buildMaterial("ML-000001", "重复编码", MaterialType.FABRIC, Map.of());

        BizException ex = assertThrows(BizException.class,
                () -> materialDomainService.createMaterial(material));
        assertEquals(ErrorCode.MATERIAL_CODE_DUPLICATE.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("创建物料 — 未填必填属性应提示具体缺少哪些字段")
    void createMaterial_missingRequired_shouldTellWhichFields() {
        when(materialRepository.existsByCode("ML-000001")).thenReturn(false);
        when(attributeDefRepository.selectByMaterialType(MaterialType.FABRIC))
                .thenReturn(buildFabricAttributeDefs());

        // ext_attrs 中缺少 weight（克重）和 width（门幅），都是必填
        Map<String, Object> extAttrs = Map.of(
                "yarnCount", "40S/1"
        );

        Material material = buildMaterial("ML-000001", "缺必填项", MaterialType.FABRIC, extAttrs);

        BizException ex = assertThrows(BizException.class,
                () -> materialDomainService.createMaterial(material));
        assertTrue(ex.getMessage().contains("克重"), "提示中应包含'克重': " + ex.getMessage());
        assertTrue(ex.getMessage().contains("门幅"), "提示中应包含'门幅': " + ex.getMessage());
    }

    // ==================== 成分百分比校验 ====================

    @Test
    @DisplayName("成分百分比合计不等于100% → 抛异常")
    void validateComposition_not100_shouldThrow() {
        Map<String, Object> extAttrs = Map.of(
                "weight", 280,
                "width", 150,
                "composition", List.of(
                        Map.of("fiber", "棉", "percentage", 60),
                        Map.of("fiber", "涤纶", "percentage", 30)
                )
        );

        when(materialRepository.existsByCode("ML-000001")).thenReturn(false);
        when(attributeDefRepository.selectByMaterialType(MaterialType.FABRIC))
                .thenReturn(buildFabricAttributeDefs());

        Material material = buildMaterial("ML-000001", "成分不足", MaterialType.FABRIC, extAttrs);

        BizException ex = assertThrows(BizException.class,
                () -> materialDomainService.createMaterial(material));
        assertEquals(ErrorCode.COMPOSITION_PERCENT_INVALID.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("成分百分比合计等于100% → 校验通过")
    void validateComposition_exactly100_shouldPass() {
        Map<String, Object> extAttrs = Map.of(
                "weight", 280,
                "width", 150,
                "composition", List.of(
                        Map.of("fiber", "棉", "percentage", 80),
                        Map.of("fiber", "涤纶", "percentage", 20)
                )
        );

        when(materialRepository.existsByCode("ML-000001")).thenReturn(false);
        when(attributeDefRepository.selectByMaterialType(MaterialType.FABRIC))
                .thenReturn(buildFabricAttributeDefs());
        when(materialRepository.insert(any())).thenReturn(1);

        Material material = buildMaterial("ML-000001", "成分正确", MaterialType.FABRIC, extAttrs);
        assertDoesNotThrow(() -> materialDomainService.createMaterial(material));
    }

    @Test
    @DisplayName("无 composition 键 → 不触发成分校验")
    void validateComposition_noCompositionKey_shouldSkip() {
        Map<String, Object> extAttrs = Map.of(
                "spec", "4孔 18mm",
                "material", "树脂"
        );

        when(materialRepository.existsByCode("ML-000002")).thenReturn(false);
        when(attributeDefRepository.selectByMaterialType(MaterialType.TRIM))
                .thenReturn(buildTrimAttributeDefs());
        when(materialRepository.insert(any())).thenReturn(1);

        Material material = buildMaterial("ML-000002", "无成分", MaterialType.TRIM, extAttrs);
        assertDoesNotThrow(() -> materialDomainService.createMaterial(material));
    }

    // ==================== 停用物料 ====================

    @Test
    @DisplayName("停用物料 — 成功")
    void deactivateMaterial_shouldSucceed() {
        Material existing = buildMaterial("ML-000001", "测试", MaterialType.FABRIC, Map.of());
        when(materialRepository.selectById(1L)).thenReturn(existing);
        when(materialRepository.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> materialDomainService.deactivateMaterial(1L));
        assertEquals(CommonStatus.INACTIVE, existing.getStatus());
    }

    @Test
    @DisplayName("停用物料 — 不存在应抛异常")
    void deactivateMaterial_notFound_shouldThrow() {
        when(materialRepository.selectById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> materialDomainService.deactivateMaterial(999L));
        assertEquals(ErrorCode.DATA_NOT_FOUND.getCode(), ex.getCode());
    }

    // ==================== 辅助方法 ====================

    private Material buildMaterial(String code, String name, MaterialType type,
                                    Map<String, Object> extAttrs) {
        Material material = new Material();
        material.setCode(code);
        material.setName(name);
        material.setType(type);
        material.setUnit("米");
        material.setExtAttrs(extAttrs);
        material.setStatus(CommonStatus.ACTIVE);
        return material;
    }

    /**
     * 构建面料属性定义列表（模拟数据库查询结果）
     */
    private List<AttributeDef> buildFabricAttributeDefs() {
        return List.of(
                buildAttrDef(101L, "fabric_weight", "克重", MaterialType.FABRIC,
                        InputType.NUMBER, true, "weight"),
                buildAttrDef(102L, "fabric_width", "门幅", MaterialType.FABRIC,
                        InputType.NUMBER, true, "width"),
                buildAttrDef(103L, "fabric_composition", "成分", MaterialType.FABRIC,
                        InputType.COMPOSITION, true, "composition"),
                buildAttrDef(104L, "fabric_yarn_count", "纱支", MaterialType.FABRIC,
                        InputType.TEXT, false, "yarnCount")
        );
    }

    /**
     * 构建辅料属性定义列表
     */
    private List<AttributeDef> buildTrimAttributeDefs() {
        return List.of(
                buildAttrDef(201L, "trim_spec", "规格", MaterialType.TRIM,
                        InputType.TEXT, true, "spec"),
                buildAttrDef(202L, "trim_material", "材质", MaterialType.TRIM,
                        InputType.TEXT, true, "material"),
                buildAttrDef(203L, "trim_color", "颜色", MaterialType.TRIM,
                        InputType.TEXT, false, "color")
        );
    }

    /**
     * 构建包材属性定义列表
     */
    private List<AttributeDef> buildPackagingAttributeDefs() {
        return List.of(
                buildAttrDef(301L, "packaging_spec", "规格", MaterialType.PACKAGING,
                        InputType.TEXT, true, "spec"),
                buildAttrDef(302L, "packaging_material", "材质", MaterialType.PACKAGING,
                        InputType.TEXT, true, "material")
        );
    }

    private AttributeDef buildAttrDef(Long id, String code, String name,
                                       MaterialType materialType, InputType inputType,
                                       boolean required, String extJsonPath) {
        AttributeDef def = new AttributeDef();
        def.setId(id);
        def.setCode(code);
        def.setName(name);
        def.setMaterialType(materialType);
        def.setInputType(inputType);
        def.setRequired(required);
        def.setExtJsonPath(extJsonPath);
        return def;
    }
}
