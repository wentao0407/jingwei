package com.jingwei.order.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.order.application.dto.CreateProductionOrderDTO;
import com.jingwei.order.application.dto.FireProductionLineEventDTO;
import com.jingwei.order.application.dto.FireProductionOrderEventDTO;
import com.jingwei.order.application.dto.ProductionOrderQueryDTO;
import com.jingwei.order.application.dto.UpdateProductionOrderDTO;
import com.jingwei.order.application.service.ProductionOrderApplicationService;
import com.jingwei.order.interfaces.vo.ProductionOrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 生产订单 Controller
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductionOrderController {

    private final ProductionOrderApplicationService productionOrderApplicationService;

    /**
     * 创建生产订单
     */
    @RequirePermission("order:production:create")
    @PostMapping("/order/production/create")
    public R<ProductionOrderVO> createProductionOrder(@Valid @RequestBody CreateProductionOrderDTO dto) {
        return R.ok(productionOrderApplicationService.createProductionOrder(dto));
    }

    /**
     * 编辑生产订单
     */
    @RequirePermission("order:production:update")
    @PostMapping("/order/production/update")
    public R<ProductionOrderVO> updateProductionOrder(@RequestParam Long orderId,
                                                       @Valid @RequestBody UpdateProductionOrderDTO dto) {
        return R.ok(productionOrderApplicationService.updateProductionOrder(orderId, dto));
    }

    /**
     * 删除生产订单
     */
    @RequirePermission("order:production:delete")
    @PostMapping("/order/production/delete")
    public R<Void> deleteProductionOrder(@RequestParam Long orderId) {
        productionOrderApplicationService.deleteProductionOrder(orderId);
        return R.ok();
    }

    /**
     * 查询生产订单详情
     */
    @PostMapping("/order/production/detail")
    public R<ProductionOrderVO> getDetail(@RequestParam Long orderId) {
        return R.ok(productionOrderApplicationService.getDetail(orderId));
    }

    /**
     * 分页查询生产订单
     */
    @PostMapping("/order/production/page")
    public R<IPage<ProductionOrderVO>> pageQuery(@Valid @RequestBody ProductionOrderQueryDTO dto) {
        return R.ok(productionOrderApplicationService.pageQuery(dto));
    }

    // ==================== 状态机流转接口（T-23） ====================

    /**
     * 触发主表状态转移
     * <p>
     * 仅支持整单事件（RELEASE、PLAN），行级事件请使用 fire-line-event 接口。
     * </p>
     */
    @RequirePermission("order:production:fire-event")
    @PostMapping("/order/production/fire-event")
    public R<Void> fireOrderEvent(@Valid @RequestBody FireProductionOrderEventDTO dto) {
        productionOrderApplicationService.fireOrderEvent(dto);
        return R.ok();
    }

    /**
     * 触发行级别状态转移
     * <p>
     * 支持行级事件（START_CUTTING、START_SEWING、START_FINISHING、COMPLETE、STOCK_IN）。
     * 行状态变更后自动重新计算主表状态（取所有行最滞后状态）。
     * </p>
     */
    @RequirePermission("order:production:fire-line-event")
    @PostMapping("/order/production/fire-line-event")
    public R<Void> fireLineEvent(@Valid @RequestBody FireProductionLineEventDTO dto) {
        productionOrderApplicationService.fireLineEvent(dto);
        return R.ok();
    }

    /**
     * 查询主表可用操作
     * <p>
     * 前端据此动态渲染操作按钮。
     * </p>
     */
    @PostMapping("/order/production/available-actions")
    public R<List<Map<String, String>>> getAvailableActions(@RequestParam Long orderId) {
        return R.ok(productionOrderApplicationService.getAvailableActions(orderId));
    }

    /**
     * 查询行可用操作
     * <p>
     * 前端据此渲染行级别的操作按钮。
     * </p>
     */
    @PostMapping("/order/production/line-available-actions")
    public R<List<Map<String, String>>> getLineAvailableActions(@RequestParam Long orderId,
                                                                  @RequestParam Long lineId) {
        return R.ok(productionOrderApplicationService.getLineAvailableActions(orderId, lineId));
    }
}
