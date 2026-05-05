package com.jingwei.cost.interfaces.controller;

import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.cost.application.service.CostApplicationService;
import com.jingwei.cost.interfaces.vo.CostMaterialIssueVO;
import com.jingwei.cost.interfaces.vo.CostProductionOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 成本核算 Controller
 * <p>
 * 提供成本查询接口。成本记录由领料出库和生产入库自动触发，不对外暴露写入接口。
 * </p>
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class CostController {

    private final CostApplicationService costApplicationService;

    /**
     * 查询生产订单成本详情
     */
    @RequirePermission("cost:query:detail")
    @PostMapping("/cost/detail")
    public R<CostProductionOrderVO> getCostDetail(@RequestParam Long productionOrderId,
                                                    @RequestParam Long productionLineId) {
        return R.ok(costApplicationService.getCostDetail(productionOrderId, productionLineId));
    }

    /**
     * 查询生产订单领料成本明细
     */
    @RequirePermission("cost:query:detail")
    @PostMapping("/cost/issues")
    public R<List<CostMaterialIssueVO>> getIssueDetails(@RequestParam Long productionOrderId) {
        return R.ok(costApplicationService.getIssueDetails(productionOrderId));
    }
}
