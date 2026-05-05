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
     * 创建波次并生成拣货单（逐单拣模式）
     * <p>
     * 为每个出库单生成一个拣货单，拣货项按库位排序。
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

        wave.setStatus(WaveStatus.DRAFT);
        waveRepository.insert(wave);

        // 为每个出库单生成拣货单
        List<PickList> pickLists = new ArrayList<>();
        for (Long outboundId : outboundOrderIds) {
            OutboundOrder outbound = outboundOrderRepository.selectDetailById(outboundId);
            if (outbound == null) {
                throw new BizException(ErrorCode.DATA_NOT_FOUND, "出库单不存在: " + outboundId);
            }
            if (outbound.getStatus() != OutboundStatus.CONFIRMED && outbound.getStatus() != OutboundStatus.PICKING) {
                throw new BizException(ErrorCode.OUTBOUND_STATUS_INVALID,
                        "出库单状态不允许加入波次: " + outbound.getOutboundNo());
            }

            // 生成拣货单
            PickList pickList = new PickList();
            pickList.setWaveId(wave.getId());
            pickList.setStatus(PickListStatus.PICKING);
            pickListRepository.insert(pickList);

            // 为每个出库单行生成拣货项
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
        log.info("创建波次: waveId={}, 出库单数={}, 拣货单数={}", wave.getId(), outboundOrderIds.size(), pickLists.size());
        return wave;
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
     * 取消波次（释放锁定库存）
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
