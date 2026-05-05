package com.jingwei.warehouse.interfaces.controller;

import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.warehouse.application.dto.ConfirmPutawayDTO;
import com.jingwei.warehouse.application.dto.ConfirmReceiveDTO;
import com.jingwei.warehouse.application.dto.CreateReceivingDTO;
import com.jingwei.warehouse.application.service.ReceivingApplicationService;
import com.jingwei.warehouse.interfaces.vo.ReceivingOrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 收货作业 Controller
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class ReceivingController {

    private final ReceivingApplicationService receivingApplicationService;

    /** 从 ASN 创建收货单 */
    @PostMapping("/warehouse/receiving/create")
    public R<ReceivingOrderVO> createFromAsn(@Valid @RequestBody CreateReceivingDTO dto) {
        return R.ok(receivingApplicationService.createFromAsn(dto));
    }

    /** 确认收货（逐行） */
    @PostMapping("/warehouse/receiving/confirm")
    public R<Void> confirmReceive(@Valid @RequestBody ConfirmReceiveDTO dto) {
        receivingApplicationService.confirmReceive(dto);
        return R.ok();
    }

    /** 推荐上架库位 */
    @PostMapping("/warehouse/receiving/suggest-locations")
    public R<List<Map<String, Object>>> suggestLocations(@RequestParam Long receivingLineId) {
        return R.ok(receivingApplicationService.suggestLocations(receivingLineId));
    }

    /** 确认上架 */
    @PostMapping("/warehouse/receiving/putaway")
    public R<Void> confirmPutaway(@Valid @RequestBody ConfirmPutawayDTO dto) {
        receivingApplicationService.confirmPutaway(dto);
        return R.ok();
    }

    /** 查询收货单详情 */
    @PostMapping("/warehouse/receiving/detail")
    public R<ReceivingOrderVO> getDetail(@RequestParam Long receivingId) {
        return R.ok(receivingApplicationService.getDetail(receivingId));
    }
}
