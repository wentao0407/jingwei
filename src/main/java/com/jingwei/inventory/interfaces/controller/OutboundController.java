package com.jingwei.inventory.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.inventory.application.dto.CreateOutboundDTO;
import com.jingwei.inventory.application.dto.OutboundQueryDTO;
import com.jingwei.inventory.application.service.OutboundApplicationService;
import com.jingwei.inventory.interfaces.vo.OutboundOrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 出库单 Controller
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundApplicationService outboundApplicationService;

    /** 创建出库单 */
    @RequirePermission("inventory:outbound:create")
    @PostMapping("/inventory/outbound/create")
    public R<OutboundOrderVO> createOutbound(@Valid @RequestBody CreateOutboundDTO dto) {
        return R.ok(outboundApplicationService.createOutbound(dto));
    }

    /** 确认出库（发货确认，扣减库存） */
    @RequirePermission("inventory:outbound:confirm")
    @PostMapping("/inventory/outbound/confirm")
    public R<Void> confirmShipped(@RequestParam Long outboundId) {
        outboundApplicationService.confirmShipped(outboundId);
        return R.ok();
    }

    /** 查询出库单详情 */
    @PostMapping("/inventory/outbound/detail")
    public R<OutboundOrderVO> getDetail(@RequestParam Long outboundId) {
        return R.ok(outboundApplicationService.getDetail(outboundId));
    }

    /** 分页查询出库单 */
    @PostMapping("/inventory/outbound/page")
    public R<IPage<OutboundOrderVO>> pageQuery(@Valid @RequestBody OutboundQueryDTO dto) {
        return R.ok(outboundApplicationService.pageQuery(dto));
    }
}
