package com.jingwei.warehouse.application.service;

import com.jingwei.common.domain.model.UserContext;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.warehouse.application.dto.ConfirmPickDTO;
import com.jingwei.warehouse.application.dto.CreateWaveDTO;
import com.jingwei.warehouse.domain.model.Wave;
import com.jingwei.warehouse.domain.model.WaveStrategy;
import com.jingwei.warehouse.domain.service.WaveDomainService;
import com.jingwei.warehouse.domain.repository.WaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 波次应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WaveApplicationService {

    /** 编码规则键：波次编号 */
    private static final String WAVE_NO_RULE = "WAVE_NO";

    private final WaveDomainService waveDomainService;
    private final WaveRepository waveRepository;
    private final CodingRuleDomainService codingRuleDomainService;

    /**
     * 创建波次
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createWave(CreateWaveDTO dto) {
        String waveNo = codingRuleDomainService.generateCode(
                WAVE_NO_RULE, java.util.Collections.emptyMap());

        Wave wave = new Wave();
        wave.setWaveNo(waveNo);
        wave.setWarehouseId(dto.getWarehouseId());
        wave.setStrategy(WaveStrategy.valueOf(dto.getStrategy()));
        wave.setRemark(dto.getRemark());

        waveDomainService.createWave(wave, dto.getOutboundOrderIds());
        return wave.getId();
    }

    /**
     * 确认拣货
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmPick(ConfirmPickDTO dto) {
        Long operatorId = UserContext.getUserId();
        waveDomainService.confirmPick(dto.getPickItemId(), dto.getActualQty(), operatorId);
    }

    /**
     * 完成拣货单（复核通过）
     */
    @Transactional(rollbackFor = Exception.class)
    public void completePickList(Long pickListId) {
        Long operatorId = UserContext.getUserId();
        waveDomainService.completePickList(pickListId, operatorId);
    }

    /**
     * 取消波次
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelWave(Long waveId) {
        Long operatorId = UserContext.getUserId();
        waveDomainService.cancelWave(waveId, operatorId);
    }
}
