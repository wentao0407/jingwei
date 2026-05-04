package com.jingwei.procurement.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.procurement.application.dto.AsnQueryDTO;
import com.jingwei.procurement.application.dto.CreateAsnDTO;
import com.jingwei.procurement.application.dto.ReceiveGoodsDTO;
import com.jingwei.procurement.application.dto.SubmitQcResultDTO;
import com.jingwei.procurement.application.service.AsnApplicationService;
import com.jingwei.procurement.interfaces.vo.AsnVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 到货通知单 Controller
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AsnController {

    private final AsnApplicationService asnApplicationService;

    /**
     * 创建到货通知单
     */
    @RequirePermission("procurement:asn:create")
    @PostMapping("/procurement/asn/create")
    public R<AsnVO> createAsn(@Valid @RequestBody CreateAsnDTO dto) {
        return R.ok(asnApplicationService.createAsn(dto));
    }

    /**
     * 确认收货
     */
    @RequirePermission("procurement:asn:receive")
    @PostMapping("/procurement/asn/receive")
    public R<Void> receiveGoods(@Valid @RequestBody ReceiveGoodsDTO dto) {
        asnApplicationService.receiveGoods(dto);
        return R.ok();
    }

    /**
     * 提交检验结果
     */
    @RequirePermission("procurement:asn:qc")
    @PostMapping("/procurement/asn/qc")
    public R<Void> submitQcResult(@Valid @RequestBody SubmitQcResultDTO dto) {
        asnApplicationService.submitQcResult(dto);
        return R.ok();
    }

    /**
     * 查询到货通知单详情
     */
    @PostMapping("/procurement/asn/detail")
    public R<AsnVO> getDetail(@RequestParam Long asnId) {
        return R.ok(asnApplicationService.getDetail(asnId));
    }

    /**
     * 分页查询到货通知单
     */
    @PostMapping("/procurement/asn/page")
    public R<IPage<AsnVO>> pageQuery(@Valid @RequestBody AsnQueryDTO dto) {
        return R.ok(asnApplicationService.pageQuery(dto));
    }
}
