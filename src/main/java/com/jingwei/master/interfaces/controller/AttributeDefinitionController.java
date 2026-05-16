package com.jingwei.master.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.master.application.dto.AttributeDefinitionQueryDTO;
import com.jingwei.master.application.dto.SaveAttributeDefinitionDTO;
import com.jingwei.master.application.service.AttributeDefinitionApplicationService;
import com.jingwei.master.interfaces.vo.AttributeDefinitionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 属性定义 Controller
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class AttributeDefinitionController {

    private final AttributeDefinitionApplicationService service;

    @RequirePermission("master:attr-def:create")
    @PostMapping("/master/attr-def/create")
    public R<AttributeDefinitionVO> create(@Valid @RequestBody SaveAttributeDefinitionDTO dto) {
        return R.ok(service.create(dto));
    }

    @RequirePermission("master:attr-def:update")
    @PostMapping("/master/attr-def/update")
    public R<AttributeDefinitionVO> update(@RequestParam Long id,
                                            @Valid @RequestBody SaveAttributeDefinitionDTO dto) {
        return R.ok(service.update(id, dto));
    }

    @RequirePermission("master:attr-def:delete")
    @PostMapping("/master/attr-def/delete")
    public R<Void> delete(@RequestParam Long id) {
        service.delete(id);
        return R.ok();
    }

    @PostMapping("/master/attr-def/detail")
    public R<AttributeDefinitionVO> getDetail(@RequestParam Long id) {
        return R.ok(service.getDetail(id));
    }

    @PostMapping("/master/attr-def/page")
    public R<IPage<AttributeDefinitionVO>> pageQuery(@Valid @RequestBody AttributeDefinitionQueryDTO dto) {
        return R.ok(service.pageQuery(dto));
    }

    @PostMapping("/master/attr-def/list-by-type")
    public R<List<AttributeDefinitionVO>> listByMaterialType(@RequestParam String materialType) {
        return R.ok(service.listByMaterialType(materialType));
    }
}
