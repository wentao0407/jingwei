package com.jingwei.inventory.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.inventory.application.dto.CreateInboundDTO;
import com.jingwei.inventory.application.dto.InboundQueryDTO;
import com.jingwei.inventory.application.service.InboundApplicationService;
import com.jingwei.inventory.interfaces.vo.InboundOrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 入库单 Controller
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class InboundController {

    private final InboundApplicationService inboundApplicationService;

    /** 创建入库单 */
    @RequirePermission("inventory:inbound:create")
    @PostMapping("/inventory/inbound/create")
    public R<InboundOrderVO> createInbound(@Valid @RequestBody CreateInboundDTO dto) {
        return R.ok(inboundApplicationService.createInbound(dto));
    }

    /** 确认入库（库存变更） */
    @RequirePermission("inventory:inbound:confirm")
    @PostMapping("/inventory/inbound/confirm")
    public R<Void> confirmInbound(@RequestParam Long inboundId) {
        inboundApplicationService.confirmInbound(inboundId);
        return R.ok();
    }

    /** 查询入库单详情 */
    @PostMapping("/inventory/inbound/detail")
    public R<InboundOrderVO> getDetail(@RequestParam Long inboundId) {
        return R.ok(inboundApplicationService.getDetail(inboundId));
    }

    /** 分页查询入库单 */
    @PostMapping("/inventory/inbound/page")
    public R<IPage<InboundOrderVO>> pageQuery(@Valid @RequestBody InboundQueryDTO dto) {
        return R.ok(inboundApplicationService.pageQuery(dto));
    }
}
