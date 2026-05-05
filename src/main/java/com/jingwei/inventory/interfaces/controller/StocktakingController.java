package com.jingwei.inventory.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.inventory.application.dto.CreateStocktakingDTO;
import com.jingwei.inventory.application.dto.RecordCountDTO;
import com.jingwei.inventory.application.dto.StocktakingQueryDTO;
import com.jingwei.inventory.application.service.StocktakingApplicationService;
import com.jingwei.inventory.interfaces.vo.StocktakingOrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 盘点单 Controller
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class StocktakingController {

    private final StocktakingApplicationService stocktakingApplicationService;

    /** 创建盘点单 */
    @RequirePermission("inventory:stocktaking:create")
    @PostMapping("/inventory/stocktaking/create")
    public R<StocktakingOrderVO> createStocktaking(@Valid @RequestBody CreateStocktakingDTO dto) {
        return R.ok(stocktakingApplicationService.createStocktaking(dto));
    }

    /** 开始盘点（冻结库位） */
    @RequirePermission("inventory:stocktaking:submit")
    @PostMapping("/inventory/stocktaking/start")
    public R<Void> startStocktaking(@RequestParam Long stocktakingId) {
        stocktakingApplicationService.startStocktaking(stocktakingId);
        return R.ok();
    }

    /** 录入实盘数量 */
    @PostMapping("/inventory/stocktaking/record-count")
    public R<Void> recordCount(@Valid @RequestBody RecordCountDTO dto) {
        stocktakingApplicationService.recordCount(dto);
        return R.ok();
    }

    /** 提交盘点结果（进入差异审核） */
    @RequirePermission("inventory:stocktaking:submit")
    @PostMapping("/inventory/stocktaking/submit")
    public R<Void> submitStocktaking(@RequestParam Long stocktakingId) {
        stocktakingApplicationService.submitStocktaking(stocktakingId);
        return R.ok();
    }

    /** 审核差异并调整库存（解冻库位） */
    @RequirePermission("inventory:stocktaking:review")
    @PostMapping("/inventory/stocktaking/review")
    public R<Void> reviewAndAdjust(@RequestParam Long stocktakingId) {
        stocktakingApplicationService.reviewAndAdjust(stocktakingId);
        return R.ok();
    }

    /** 查询盘点单详情（支持盲盘模式） */
    @PostMapping("/inventory/stocktaking/detail")
    public R<StocktakingOrderVO> getDetail(@RequestParam Long stocktakingId) {
        return R.ok(stocktakingApplicationService.getDetail(stocktakingId));
    }

    /** 分页查询盘点单 */
    @PostMapping("/inventory/stocktaking/page")
    public R<IPage<StocktakingOrderVO>> pageQuery(@Valid @RequestBody StocktakingQueryDTO dto) {
        return R.ok(stocktakingApplicationService.pageQuery(dto));
    }
}
