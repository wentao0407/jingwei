package com.jingwei.warehouse.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.warehouse.application.dto.CreateMaterialIssueDTO;
import com.jingwei.warehouse.application.dto.MaterialIssueQueryDTO;
import com.jingwei.warehouse.application.service.MaterialIssueApplicationService;
import com.jingwei.warehouse.interfaces.vo.MaterialIssueVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 领料出库 Controller
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class MaterialIssueController {

    private final MaterialIssueApplicationService materialIssueApplicationService;

    @RequirePermission("warehouse:material-issue:create")
    @PostMapping("/warehouse/material-issue/create")
    public R<MaterialIssueVO> createIssue(@Valid @RequestBody CreateMaterialIssueDTO dto) {
        return R.ok(materialIssueApplicationService.createIssue(dto));
    }

    @RequirePermission("warehouse:material-issue:confirm")
    @PostMapping("/warehouse/material-issue/confirm")
    public R<Void> confirmIssue(@RequestParam Long issueId) {
        materialIssueApplicationService.confirmIssue(issueId);
        return R.ok();
    }

    @PostMapping("/warehouse/material-issue/detail")
    public R<MaterialIssueVO> getDetail(@RequestParam Long issueId) {
        return R.ok(materialIssueApplicationService.getDetail(issueId));
    }

    @PostMapping("/warehouse/material-issue/page")
    public R<IPage<MaterialIssueVO>> pageQuery(@Valid @RequestBody MaterialIssueQueryDTO dto) {
        return R.ok(materialIssueApplicationService.pageQuery(dto));
    }
}
