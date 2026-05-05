package com.jingwei.warehouse.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.model.OutboundOrder;
import com.jingwei.inventory.domain.model.OutboundOrderLine;
import com.jingwei.inventory.domain.model.OutboundStatus;
import com.jingwei.inventory.domain.repository.OutboundOrderLineRepository;
import com.jingwei.inventory.domain.repository.OutboundOrderRepository;
import com.jingwei.warehouse.domain.model.*;
import com.jingwei.warehouse.domain.repository.PickItemRepository;
import com.jingwei.warehouse.domain.repository.PickListRepository;
import com.jingwei.warehouse.domain.repository.WaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 波次领域服务
 * <p>
 * 负责波次创建、拣货单生成、拣货确认、复核的完整出库作业流程。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WaveDomainService {

    private final WaveRepository waveRepository;
    private final PickListRepository pickListRepository;
    private final PickItemRepository pickItemRepository;
    private final OutboundOrderRepository outboundOrderRepository;
    private final OutboundOrderLineRepository outboundOrderLineRepository;

    /**
     * 创建波次（草稿状态）
     * <p>
     * 仅创建波次记录，关联出库单，不生成拣货单。
     * 拣货单在释放波次时生成。
     * </p>
     *
     * @param wave             波次实体（waveNo 由 ApplicationService 生成后设置）
     * @param outboundOrderIds 出库单ID列表
     * @return 保存后的波次
     */
    public Wave createWave(Wave wave, List<Long> outboundOrderIds) {
        if (outboundOrderIds == null || outboundOrderIds.isEmpty()) {
            throw new BizException(ErrorCode.ORDER_LINE_EMPTY, "至少选择一张出库单");
        }

        // 校验出库单状态
        for (Long outboundId : outboundOrderIds) {
            OutboundOrder outbound = outboundOrderRepository.selectDetailById(outboundId);
            if (outbound == null) {
                throw new BizException(ErrorCode.DATA_NOT_FOUND, "出库单不存在: " + outboundId);
            }
            if (outbound.getStatus() != OutboundStatus.CONFIRMED) {
                throw new BizException(ErrorCode.OUTBOUND_STATUS_INVALID,
                        "出库单状态不允许加入波次: " + outbound.getOutboundNo());
            }
        }

        wave.setStatus(WaveStatus.DRAFT);
        waveRepository.insert(wave);

        log.info("创建波次（草稿）: waveId={}, 出库单数={}", wave.getId(), outboundOrderIds.size());
        return wave;
    }

    /**
     * 释放波次（DRAFT → RELEASED → PICKING）
     * <p>
     * 释放时为每个出库单生成拣货单和拣货项，分配库位/批次，
     * 然后将波次推进到拣货中状态。
     * </p>
     *
     * @param waveId     波次ID
     * @param outboundOrderIds 出库单ID列表
     * @param operatorId 操作人ID
     */
    public void releaseWave(Long waveId, List<Long> outboundOrderIds, Long operatorId) {
        Wave wave = waveRepository.selectById(waveId);
        if (wave == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "波次不存在");
        }
        if (wave.getStatus() != WaveStatus.DRAFT) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "只有草稿状态的波次允许释放");
        }

        // 为每个出库单生成拣货单
        List<PickList> pickLists = new ArrayList<>();
        for (Long outboundId : outboundOrderIds) {
            OutboundOrder outbound = outboundOrderRepository.selectDetailById(outboundId);
            if (outbound == null) {
                throw new BizException(ErrorCode.DATA_NOT_FOUND, "出库单不存在: " + outboundId);
            }

            PickList pickList = new PickList();
            pickList.setWaveId(wave.getId());
            pickList.setStatus(PickListStatus.PICKING);
            pickListRepository.insert(pickList);

            List<PickItem> items = new ArrayList<>();
            for (OutboundOrderLine line : outbound.getLines()) {
                PickItem item = new PickItem();
                item.setPickListId(pickList.getId());
                item.setOutboundLineId(line.getId());
                item.setSkuId(line.getSkuId());
                item.setLocationId(line.getLocationId());
                item.setBatchNo(line.getBatchNo());
                item.setPlannedQty(line.getActualQty() != null ? line.getActualQty() : line.getPlannedQty());
                item.setStatus(PickItemStatus.PICKING);
                items.add(item);
            }
            pickItemRepository.batchInsert(items);
            pickList.setItems(items);
            pickLists.add(pickList);
        }

        wave.setPickLists(pickLists);
        wave.setStatus(WaveStatus.PICKING);
        waveRepository.updateById(wave);

        log.info("波次已释放: waveId={}, 拣货单数={}", waveId, pickLists.size());
    }

    /**
     * 开始分拣复核（PICKING → SORTING）
     * <p>
     * 所有拣货单完成后，波次进入分拣复核阶段。
     * </p>
     *
     * @param waveId     波次ID
     * @param operatorId 操作人ID
     */
    public void startSorting(Long waveId, Long operatorId) {
        Wave wave = waveRepository.selectById(waveId);
        if (wave == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "波次不存在");
        }
        if (wave.getStatus() != WaveStatus.PICKING) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "只有拣货中的波次允许开始分拣");
        }

        // 检查是否所有拣货单都已完成
        List<PickList> pickLists = pickListRepository.selectByWaveId(waveId);
        boolean allCompleted = pickLists.stream()
                .allMatch(pl -> pl.getStatus() == PickListStatus.COMPLETED || pl.getStatus() == PickListStatus.DISCREPANCY);
        if (!allCompleted) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "存在未完成的拣货单，不可开始分拣复核");
        }

        wave.setStatus(WaveStatus.SORTING);
        waveRepository.updateById(wave);

        log.info("波次进入分拣复核: waveId={}", waveId);
    }

    /**
     * 确认拣货（逐项）
     */
    public void confirmPick(Long pickItemId, BigDecimal actualQty, Long operatorId) {
        PickItem item = pickItemRepository.selectById(pickItemId);
        if (item == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "拣货项不存在");
        }

        item.setActualQty(actualQty);

        // 短拣检测
        if (actualQty.compareTo(item.getPlannedQty()) < 0) {
            item.setStatus(PickItemStatus.SHORT);
            log.warn("短拣: pickItemId={}, planned={}, actual={}", pickItemId, item.getPlannedQty(), actualQty);
        } else {
            item.setStatus(PickItemStatus.COMPLETED);
        }

        pickItemRepository.updateById(item);
    }

    /**
     * 完成拣货单（复核通过）
     */
    public void completePickList(Long pickListId, Long operatorId) {
        PickList pickList = pickListRepository.selectById(pickListId);
        if (pickList == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "拣货单不存在");
        }

        List<PickItem> items = pickItemRepository.selectByPickListId(pickListId);
        boolean hasShort = items.stream().anyMatch(i -> i.getStatus() == PickItemStatus.SHORT);
        boolean hasPending = items.stream().anyMatch(i -> i.getStatus() == PickItemStatus.PICKING);

        if (hasPending) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "有未拣货的项目，不可完成复核");
        }

        pickList.setStatus(hasShort ? PickListStatus.DISCREPANCY : PickListStatus.COMPLETED);
        pickListRepository.updateById(pickList);

        log.info("拣货单完成: pickListId={}, status={}", pickListId, pickList.getStatus());
    }

    /**
     * 完成波次（SORTING → COMPLETED）
     * <p>
     * 分拣复核完成后，波次进入完成状态。
     * </p>
     *
     * @param waveId     波次ID
     * @param operatorId 操作人ID
     */
    public void completeWave(Long waveId, Long operatorId) {
        Wave wave = waveRepository.selectById(waveId);
        if (wave == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "波次不存在");
        }
        if (wave.getStatus() != WaveStatus.SORTING) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "只有分拣复核中的波次允许完成");
        }

        wave.setStatus(WaveStatus.COMPLETED);
        waveRepository.updateById(wave);

        log.info("波次已完成: waveId={}", waveId);
    }

    /**
     * 取消波次
     */
    public void cancelWave(Long waveId, Long operatorId) {
        Wave wave = waveRepository.selectById(waveId);
        if (wave == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "波次不存在");
        }
        if (wave.getStatus() != WaveStatus.DRAFT && wave.getStatus() != WaveStatus.PICKING) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "只有草稿或拣货中的波次允许取消");
        }

        wave.setStatus(WaveStatus.CANCELLED);
        waveRepository.updateById(wave);

        log.info("波次已取消: waveId={}", waveId);
    }
}
