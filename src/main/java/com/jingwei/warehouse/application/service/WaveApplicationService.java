package com.jingwei.warehouse.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.UserContext;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.warehouse.application.dto.ConfirmPickDTO;
import com.jingwei.warehouse.application.dto.CreateWaveDTO;
import com.jingwei.warehouse.application.dto.WaveQueryDTO;
import com.jingwei.warehouse.domain.model.PickItem;
import com.jingwei.warehouse.domain.model.PickList;
import com.jingwei.warehouse.domain.model.Wave;
import com.jingwei.warehouse.domain.model.WaveStrategy;
import com.jingwei.warehouse.domain.model.WaveStatus;
import com.jingwei.warehouse.domain.service.WaveDomainService;
import com.jingwei.warehouse.domain.repository.WaveRepository;
import com.jingwei.warehouse.interfaces.vo.PickItemVO;
import com.jingwei.warehouse.interfaces.vo.PickListVO;
import com.jingwei.warehouse.interfaces.vo.WaveVO;
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

    /**
     * 编码规则键：波次编号。
     * 与 t_md_coding_rule.code = 'WAVE_NO' 对应，用于创建波次时自动生成波次编号。
     */
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

    public WaveVO getDetail(Long waveId) {
        return toVO(waveRepository.selectDetailById(waveId));
    }

    public Page<WaveVO> pageQuery(WaveQueryDTO dto) {
        WaveStatus status = dto.getStatus() == null || dto.getStatus().isBlank()
                ? null
                : WaveStatus.valueOf(dto.getStatus());
        Page<Wave> page = new Page<>(dto.getCurrent(), dto.getSize());
        Page<Wave> result = waveRepository.selectPage(page, dto.getWarehouseId(), status, dto.getWaveNo());
        Page<WaveVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    private WaveVO toVO(Wave wave) {
        if (wave == null) {
            return null;
        }
        WaveVO vo = new WaveVO();
        vo.setId(wave.getId());
        vo.setWaveNo(wave.getWaveNo());
        vo.setWarehouseId(wave.getWarehouseId());
        vo.setStrategy(wave.getStrategy() == null ? null : wave.getStrategy().name());
        vo.setStatus(wave.getStatus() == null ? null : wave.getStatus().name());
        vo.setRemark(wave.getRemark());
        vo.setCreatedAt(wave.getCreatedAt());
        vo.setPickLists(wave.getPickLists() == null ? null : wave.getPickLists().stream().map(this::toPickListVO).toList());
        return vo;
    }

    private PickListVO toPickListVO(PickList pickList) {
        PickListVO vo = new PickListVO();
        vo.setId(pickList.getId());
        vo.setWaveId(pickList.getWaveId());
        vo.setPickListNo(pickList.getPickListNo());
        vo.setPickerId(pickList.getPickerId());
        vo.setStatus(pickList.getStatus() == null ? null : pickList.getStatus().name());
        vo.setRemark(pickList.getRemark());
        vo.setItems(pickList.getItems() == null ? null : pickList.getItems().stream().map(this::toPickItemVO).toList());
        return vo;
    }

    private PickItemVO toPickItemVO(PickItem item) {
        PickItemVO vo = new PickItemVO();
        vo.setId(item.getId());
        vo.setPickListId(item.getPickListId());
        vo.setOutboundLineId(item.getOutboundLineId());
        vo.setSkuId(item.getSkuId());
        vo.setLocationId(item.getLocationId());
        vo.setBatchNo(item.getBatchNo());
        vo.setPlannedQty(item.getPlannedQty());
        vo.setActualQty(item.getActualQty());
        vo.setStatus(item.getStatus() == null ? null : item.getStatus().name());
        vo.setRemark(item.getRemark());
        return vo;
    }
}
