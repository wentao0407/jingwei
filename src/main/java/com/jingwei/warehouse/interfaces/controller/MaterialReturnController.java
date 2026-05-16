package com.jingwei.warehouse.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.warehouse.application.dto.CreateMaterialReturnDTO;
import com.jingwei.warehouse.application.dto.MaterialReturnQueryDTO;
import com.jingwei.warehouse.application.service.MaterialReturnApplicationService;
import com.jingwei.warehouse.interfaces.vo.MaterialReturnVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MaterialReturnController {

    private final MaterialReturnApplicationService materialReturnApplicationService;

    @RequirePermission("warehouse:material-return:create")
    @PostMapping("/warehouse/material-return/create")
    public R<MaterialReturnVO> createReturn(@Valid @RequestBody CreateMaterialReturnDTO dto) {
        return R.ok(materialReturnApplicationService.createReturn(dto));
    }

    @RequirePermission("warehouse:material-return:confirm")
    @PostMapping("/warehouse/material-return/confirm")
    public R<Void> confirmReturn(@RequestParam Long returnId) {
        materialReturnApplicationService.confirmReturn(returnId);
        return R.ok();
    }

    @PostMapping("/warehouse/material-return/detail")
    public R<MaterialReturnVO> getDetail(@RequestParam Long returnId) {
        return R.ok(materialReturnApplicationService.getDetail(returnId));
    }

    @PostMapping("/warehouse/material-return/page")
    public R<IPage<MaterialReturnVO>> pageQuery(@Valid @RequestBody MaterialReturnQueryDTO dto) {
        return R.ok(materialReturnApplicationService.pageQuery(dto));
    }
}
