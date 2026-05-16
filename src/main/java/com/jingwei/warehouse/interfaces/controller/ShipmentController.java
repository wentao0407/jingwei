package com.jingwei.warehouse.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.inventory.interfaces.vo.OutboundOrderVO;
import com.jingwei.warehouse.application.dto.ConfirmShipmentDTO;
import com.jingwei.warehouse.application.dto.ShipmentQueryDTO;
import com.jingwei.warehouse.application.service.ShipmentApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发货 Controller
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentApplicationService shipmentApplicationService;

    /** 确认发货（扣减库存 + 更新销售订单状态） */
    @PostMapping("/warehouse/shipment/confirm")
    public R<Void> confirmShipment(@Valid @RequestBody ConfirmShipmentDTO dto) {
        shipmentApplicationService.confirmShipment(dto);
        return R.ok();
    }

    /** 分页查询发运单聚合 */
    @PostMapping("/warehouse/shipment/page")
    public R<IPage<OutboundOrderVO>> pageShipments(@Valid @RequestBody ShipmentQueryDTO dto) {
        return R.ok(shipmentApplicationService.pageShipments(dto));
    }

    /** 查询发运单详情 */
    @PostMapping("/warehouse/shipment/detail")
    public R<OutboundOrderVO> getShipmentDetail(@RequestParam Long shipmentId) {
        return R.ok(shipmentApplicationService.getShipmentDetail(shipmentId));
    }
}
