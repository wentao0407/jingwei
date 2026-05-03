package com.jingwei.order.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.order.application.dto.CreateProductionOrderDTO;
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
}
