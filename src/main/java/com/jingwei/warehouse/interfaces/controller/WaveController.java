package com.jingwei.warehouse.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.warehouse.application.dto.ConfirmPickDTO;
import com.jingwei.warehouse.application.dto.CreateWaveDTO;
import com.jingwei.warehouse.application.dto.WaveQueryDTO;
import com.jingwei.warehouse.application.service.WaveApplicationService;
import com.jingwei.warehouse.interfaces.vo.WaveVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 波次管理 Controller
 *
 * @author JingWei
 */
@RestController
@RequiredArgsConstructor
public class WaveController {

    private final WaveApplicationService waveApplicationService;

    /** 创建波次 */
    @PostMapping("/warehouse/wave/create")
    public R<Long> createWave(@Valid @RequestBody CreateWaveDTO dto) {
        return R.ok(waveApplicationService.createWave(dto));
    }

    /** 分页查询波次 */
    @PostMapping("/warehouse/wave/page")
    public R<IPage<WaveVO>> pageWaves(@Valid @RequestBody WaveQueryDTO dto) {
        return R.ok(waveApplicationService.pageQuery(dto));
    }

    /** 查询波次详情 */
    @PostMapping("/warehouse/wave/detail")
    public R<WaveVO> getDetail(@RequestParam Long waveId) {
        return R.ok(waveApplicationService.getDetail(waveId));
    }

    /** 确认拣货（逐项） */
    @PostMapping("/warehouse/wave/confirm-pick")
    public R<Void> confirmPick(@Valid @RequestBody ConfirmPickDTO dto) {
        waveApplicationService.confirmPick(dto);
        return R.ok();
    }

    /** 完成拣货单（复核通过） */
    @PostMapping("/warehouse/wave/complete-pick-list")
    public R<Void> completePickList(@RequestParam Long pickListId) {
        waveApplicationService.completePickList(pickListId);
        return R.ok();
    }

    /** 取消波次 */
    @PostMapping("/warehouse/wave/cancel")
    public R<Void> cancelWave(@RequestParam Long waveId) {
        waveApplicationService.cancelWave(waveId);
        return R.ok();
    }
}
