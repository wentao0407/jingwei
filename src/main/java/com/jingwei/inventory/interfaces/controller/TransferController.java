package com.jingwei.inventory.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.inventory.application.dto.CreateTransferDTO;
import com.jingwei.inventory.application.dto.TransferQueryDTO;
import com.jingwei.inventory.application.service.TransferApplicationService;
import com.jingwei.inventory.interfaces.vo.TransferOrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 调拨单 Controller
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class TransferController {

    private final TransferApplicationService transferApplicationService;

    /** 创建调拨单 */
    @RequirePermission("inventory:transfer:create")
    @PostMapping("/inventory/transfer/create")
    public R<TransferOrderVO> createTransfer(@Valid @RequestBody CreateTransferDTO dto) {
        return R.ok(transferApplicationService.createTransfer(dto));
    }

    /** 确认调拨（源仓扣减） */
    @RequirePermission("inventory:transfer:confirm")
    @PostMapping("/inventory/transfer/confirm")
    public R<Void> confirmTransfer(@RequestParam Long transferId) {
        transferApplicationService.confirmTransfer(transferId);
        return R.ok();
    }

    /** 完成调拨（目标仓增加） */
    @RequirePermission("inventory:transfer:complete")
    @PostMapping("/inventory/transfer/complete")
    public R<Void> completeTransfer(@RequestParam Long transferId) {
        transferApplicationService.completeTransfer(transferId);
        return R.ok();
    }

    /** 取消调拨 */
    @RequirePermission("inventory:transfer:cancel")
    @PostMapping("/inventory/transfer/cancel")
    public R<Void> cancelTransfer(@RequestParam Long transferId) {
        transferApplicationService.cancelTransfer(transferId);
        return R.ok();
    }

    /** 查询调拨单详情 */
    @PostMapping("/inventory/transfer/detail")
    public R<TransferOrderVO> getDetail(@RequestParam Long transferId) {
        return R.ok(transferApplicationService.getDetail(transferId));
    }

    /** 分页查询调拨单 */
    @PostMapping("/inventory/transfer/page")
    public R<IPage<TransferOrderVO>> pageQuery(@Valid @RequestBody TransferQueryDTO dto) {
        return R.ok(transferApplicationService.pageQuery(dto));
    }
}
