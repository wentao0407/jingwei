package com.jingwei.procurement.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.procurement.application.dto.BomQueryDTO;
import com.jingwei.procurement.application.dto.CreateBomDTO;
import com.jingwei.procurement.application.dto.UpdateBomDTO;
import com.jingwei.procurement.application.service.BomApplicationService;
import com.jingwei.procurement.interfaces.vo.BomVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * BOM Controller
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class BomController {

    private final BomApplicationService bomApplicationService;

    /**
     * 创建 BOM
     */
    @RequirePermission("procurement:bom:create")
    @PostMapping("/procurement/bom/create")
    public R<BomVO> createBom(@Valid @RequestBody CreateBomDTO dto) {
        return R.ok(bomApplicationService.createBom(dto));
    }

    /**
     * 编辑 BOM
     */
    @RequirePermission("procurement:bom:update")
    @PostMapping("/procurement/bom/update")
    public R<BomVO> updateBom(@RequestParam Long bomId,
                               @Valid @RequestBody UpdateBomDTO dto) {
        return R.ok(bomApplicationService.updateBom(bomId, dto));
    }

    /**
     * 删除 BOM
     */
    @RequirePermission("procurement:bom:delete")
    @PostMapping("/procurement/bom/delete")
    public R<Void> deleteBom(@RequestParam Long bomId) {
        bomApplicationService.deleteBom(bomId);
        return R.ok();
    }

    /**
     * 审批 BOM
     */
    @RequirePermission("procurement:bom:approve")
    @PostMapping("/procurement/bom/approve")
    public R<Void> approveBom(@RequestParam Long bomId) {
        bomApplicationService.approveBom(bomId);
        return R.ok();
    }

    /**
     * 查询 BOM 详情
     */
    @PostMapping("/procurement/bom/detail")
    public R<BomVO> getDetail(@RequestParam Long bomId) {
        return R.ok(bomApplicationService.getDetail(bomId));
    }

    /**
     * 分页查询 BOM
     */
    @PostMapping("/procurement/bom/page")
    public R<IPage<BomVO>> pageQuery(@Valid @RequestBody BomQueryDTO dto) {
        return R.ok(bomApplicationService.pageQuery(dto));
    }
}
