package com.jingwei.master.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.master.application.dto.CreateMaterialDTO;
import com.jingwei.master.application.dto.MaterialQueryDTO;
import com.jingwei.master.application.dto.UpdateMaterialDTO;
import com.jingwei.master.application.service.MaterialApplicationService;
import com.jingwei.master.interfaces.vo.AttributeDefVO;
import com.jingwei.master.interfaces.vo.MaterialVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 物料主数据管理 Controller
 * <p>
 * 提供物料 CRUD、分页查询、属性定义查询接口。
 * 所有接口统一使用 POST 方法。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialApplicationService materialApplicationService;

    /**
     * 创建物料（编码由后端编码规则引擎自动生成）
     */
    @RequirePermission("master:material:create")
    @PostMapping("/master/material/create")
    public R<MaterialVO> createMaterial(@Valid @RequestBody CreateMaterialDTO dto) {
        return R.ok(materialApplicationService.createMaterial(dto));
    }

    /**
     * 更新物料（编码和类型不可修改）
     */
    @RequirePermission("master:material:update")
    @PostMapping("/master/material/update")
    public R<MaterialVO> updateMaterial(@RequestParam Long materialId,
                                        @Valid @RequestBody UpdateMaterialDTO dto) {
        return R.ok(materialApplicationService.updateMaterial(materialId, dto));
    }

    /**
     * 停用物料（停用后不可在业务单据中选择）
     */
    @RequirePermission("master:material:deactivate")
    @PostMapping("/master/material/deactivate")
    public R<Void> deactivateMaterial(@RequestParam Long materialId) {
        materialApplicationService.deactivateMaterial(materialId);
        return R.ok();
    }

    /**
     * 根据ID查询物料详情
     */
    @PostMapping("/master/material/detail")
    public R<MaterialVO> getMaterialById(@RequestParam Long materialId) {
        return R.ok(materialApplicationService.getMaterialById(materialId));
    }

    /**
     * 分页查询物料（支持按类型/分类/状态/关键词筛选）
     */
    @PostMapping("/master/material/page")
    public R<IPage<MaterialVO>> pageQuery(@RequestBody MaterialQueryDTO dto) {
        return R.ok(materialApplicationService.pageQuery(dto));
    }

    /**
     * 查询指定物料类型的属性定义
     * <p>
     * 前端根据此接口渲染动态属性表单。
     * </p>
     */
    @PostMapping("/master/material/attributeDefs")
    public R<List<AttributeDefVO>> getAttributeDefs(@RequestParam String materialType) {
        return R.ok(materialApplicationService.getAttributeDefs(materialType));
    }
}
