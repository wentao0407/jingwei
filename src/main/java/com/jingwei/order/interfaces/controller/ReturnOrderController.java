package com.jingwei.order.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.order.application.dto.ReturnOrderCreateDTO;
import com.jingwei.order.application.dto.ReturnQcDTO;
import com.jingwei.order.application.service.ReturnOrderApplicationService;
import com.jingwei.order.interfaces.vo.ReturnOrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 退货管理 Controller
 * <p>
 * 提供退货单创建、审批、收货、质检等接口。
 * </p>
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class ReturnOrderController {

    private final ReturnOrderApplicationService returnOrderApplicationService;

    /**
     * 创建退货单
     */
    @RequirePermission("order:return:create")
    @PostMapping("/order/return/create")
    public R<ReturnOrderVO> createReturnOrder(@Valid @RequestBody ReturnOrderCreateDTO dto) {
        return R.ok(returnOrderApplicationService.createReturnOrder(dto));
    }

    /**
     * 分页查询退货单
     */
    @PostMapping("/order/return/page")
    public R<IPage<ReturnOrderVO>> pageReturnOrders(@Valid @RequestBody com.jingwei.order.application.dto.ReturnOrderQueryDTO dto) {
        return R.ok(returnOrderApplicationService.pageQuery(dto));
    }

    /**
     * 查询退货单详情
     */
    @PostMapping("/order/return/detail")
    public R<ReturnOrderVO> getDetail(@RequestParam Long returnId) {
        return R.ok(returnOrderApplicationService.getDetail(returnId));
    }

    /**
     * 提交退货审批
     */
    @RequirePermission("order:return:submit")
    @PostMapping("/order/return/submit")
    public R<Void> submitForApproval(@RequestParam Long returnId) {
        returnOrderApplicationService.submitForApproval(returnId);
        return R.ok();
    }

    /**
     * 审批通过
     */
    @RequirePermission("order:return:approve")
    @PostMapping("/order/return/approve")
    public R<Void> approve(@RequestParam Long returnId) {
        returnOrderApplicationService.approve(returnId);
        return R.ok();
    }

    /**
     * 审批驳回
     */
    @RequirePermission("order:return:approve")
    @PostMapping("/order/return/reject")
    public R<Void> reject(@RequestParam Long returnId) {
        returnOrderApplicationService.reject(returnId);
        return R.ok();
    }

    /**
     * 退货收货确认
     */
    @RequirePermission("order:return:receive")
    @PostMapping("/order/return/receive")
    public R<Void> confirmReceive(@RequestParam Long returnId) {
        returnOrderApplicationService.confirmReceive(returnId);
        return R.ok();
    }

    /**
     * 退货质检
     */
    @RequirePermission("order:return:qc")
    @PostMapping("/order/return/qc")
    public R<Void> processQc(@Valid @RequestBody ReturnQcDTO dto) {
        returnOrderApplicationService.processQc(dto);
        return R.ok();
    }
}
