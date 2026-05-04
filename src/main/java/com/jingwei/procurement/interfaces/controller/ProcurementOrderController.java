package com.jingwei.procurement.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.procurement.application.dto.CreateProcurementOrderDTO;
import com.jingwei.procurement.application.dto.FireProcurementOrderEventDTO;
import com.jingwei.procurement.application.dto.ProcurementOrderQueryDTO;
import com.jingwei.procurement.application.service.ProcurementOrderApplicationService;
import com.jingwei.procurement.interfaces.vo.ProcurementOrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 采购订单 Controller
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ProcurementOrderController {

    private final ProcurementOrderApplicationService procurementOrderApplicationService;

    /**
     * 创建采购订单
     */
    @RequirePermission("procurement:order:create")
    @PostMapping("/procurement/order/create")
    public R<ProcurementOrderVO> createOrder(@Valid @RequestBody CreateProcurementOrderDTO dto) {
        return R.ok(procurementOrderApplicationService.createOrder(dto));
    }

    /**
     * 查询采购订单详情
     */
    @PostMapping("/procurement/order/detail")
    public R<ProcurementOrderVO> getDetail(@RequestParam Long orderId) {
        return R.ok(procurementOrderApplicationService.getDetail(orderId));
    }

    /**
     * 分页查询采购订单
     */
    @PostMapping("/procurement/order/page")
    public R<IPage<ProcurementOrderVO>> pageQuery(@Valid @RequestBody ProcurementOrderQueryDTO dto) {
        return R.ok(procurementOrderApplicationService.pageQuery(dto));
    }

    /**
     * 触发采购订单状态转移
     */
    @RequirePermission("procurement:order:fire-event")
    @PostMapping("/procurement/order/fire-event")
    public R<Void> fireEvent(@Valid @RequestBody FireProcurementOrderEventDTO dto) {
        procurementOrderApplicationService.fireEvent(dto);
        return R.ok();
    }

    /**
     * 查询可用操作
     */
    @PostMapping("/procurement/order/available-actions")
    public R<List<String>> getAvailableActions(@RequestParam Long orderId) {
        return R.ok(procurementOrderApplicationService.getAvailableActions(orderId));
    }
}
