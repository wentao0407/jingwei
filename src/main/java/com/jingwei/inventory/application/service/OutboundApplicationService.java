package com.jingwei.inventory.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.UserContext;
import com.jingwei.inventory.application.dto.CreateOutboundDTO;
import com.jingwei.inventory.application.dto.CreateOutboundLineDTO;
import com.jingwei.inventory.application.dto.OutboundQueryDTO;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.service.OutboundDomainService;
import com.jingwei.inventory.domain.repository.OutboundOrderRepository;
import com.jingwei.inventory.interfaces.vo.OutboundOrderLineVO;
import com.jingwei.inventory.interfaces.vo.OutboundOrderVO;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 出库单应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundApplicationService {

    /** 编码规则键：出库单编号 */
    private static final String OUTBOUND_NO_RULE = "OUTBOUND_NO";

    private final OutboundDomainService outboundDomainService;
    private final OutboundOrderRepository outboundOrderRepository;
    private final CodingRuleDomainService codingRuleDomainService;

    /**
     * 创建出库单
     */
    @Transactional(rollbackFor = Exception.class)
    public OutboundOrderVO createOutbound(CreateOutboundDTO dto) {
        String outboundNo = codingRuleDomainService.generateCode(
                OUTBOUND_NO_RULE, java.util.Collections.emptyMap());

        OutboundOrder order = new OutboundOrder();
        order.setOutboundNo(outboundNo);
        order.setOutboundType(OutboundType.valueOf(dto.getOutboundType()));
        order.setWarehouseId(dto.getWarehouseId());
        order.setSourceType(dto.getSourceType());
        order.setSourceId(dto.getSourceId());
        order.setSourceNo(dto.getSourceNo());
        order.setCarrier(dto.getCarrier());
        order.setTrackingNo(dto.getTrackingNo());
        order.setRemark(dto.getRemark());

        List<OutboundOrderLine> lines = buildLines(dto.getLines());
        OutboundOrder saved = outboundDomainService.createOrder(order, lines);
        return toVO(saved);
    }

    /**
     * 确认出库（发货确认，扣减库存）
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmShipped(Long outboundId) {
        Long operatorId = UserContext.getUserId();
        outboundDomainService.confirmShipped(outboundId, operatorId);
    }

    /**
     * 查询出库单详情
     */
    public OutboundOrderVO getDetail(Long outboundId) {
        OutboundOrder order = outboundOrderRepository.selectDetailById(outboundId);
        return toVO(order);
    }

    /**
     * 分页查询
     */
    public IPage<OutboundOrderVO> pageQuery(OutboundQueryDTO dto) {
        Page<OutboundOrder> page = new Page<>(dto.getCurrent(), dto.getSize());
        OutboundStatus status = dto.getStatus() != null ? OutboundStatus.valueOf(dto.getStatus()) : null;
        IPage<OutboundOrder> result = outboundOrderRepository.selectPage(
                page, status, dto.getWarehouseId(), dto.getOutboundNo());
        return result.convert(this::toVO);
    }

    // ==================== 私有方法 ====================

    private List<OutboundOrderLine> buildLines(List<CreateOutboundLineDTO> dtos) {
        List<OutboundOrderLine> lines = new ArrayList<>();
        for (CreateOutboundLineDTO dto : dtos) {
            OutboundOrderLine line = new OutboundOrderLine();
            line.setInventoryType(InventoryType.valueOf(dto.getInventoryType()));
            line.setSkuId(dto.getSkuId());
            line.setMaterialId(dto.getMaterialId());
            line.setBatchNo(dto.getBatchNo() != null ? dto.getBatchNo() : "");
            line.setPlannedQty(dto.getPlannedQty());
            line.setActualQty(dto.getActualQty());
            line.setLocationId(dto.getLocationId());
            line.setAllocationId(dto.getAllocationId());
            line.setRemark(dto.getRemark());
            lines.add(line);
        }
        return lines;
    }

    private OutboundOrderVO toVO(OutboundOrder order) {
        if (order == null) return null;
        OutboundOrderVO vo = new OutboundOrderVO();
        vo.setId(order.getId());
        vo.setOutboundNo(order.getOutboundNo());
        vo.setOutboundType(order.getOutboundType() != null ? order.getOutboundType().getCode() : null);
        vo.setOutboundTypeLabel(order.getOutboundType() != null ? order.getOutboundType().getLabel() : null);
        vo.setWarehouseId(order.getWarehouseId());
        vo.setStatus(order.getStatus() != null ? order.getStatus().getCode() : null);
        vo.setStatusLabel(order.getStatus() != null ? order.getStatus().getLabel() : null);
        vo.setSourceType(order.getSourceType());
        vo.setSourceId(order.getSourceId());
        vo.setSourceNo(order.getSourceNo());
        vo.setOutboundDate(order.getOutboundDate() != null ? order.getOutboundDate().toString() : null);
        vo.setCarrier(order.getCarrier());
        vo.setTrackingNo(order.getTrackingNo());
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());

        if (order.getLines() != null) {
            vo.setLines(order.getLines().stream().map(this::toLineVO).toList());
        } else {
            vo.setLines(List.of());
        }
        return vo;
    }

    private OutboundOrderLineVO toLineVO(OutboundOrderLine line) {
        OutboundOrderLineVO vo = new OutboundOrderLineVO();
        vo.setId(line.getId());
        vo.setLineNo(line.getLineNo());
        vo.setInventoryType(line.getInventoryType() != null ? line.getInventoryType().getCode() : null);
        vo.setSkuId(line.getSkuId());
        vo.setMaterialId(line.getMaterialId());
        vo.setBatchNo(line.getBatchNo());
        vo.setPlannedQty(line.getPlannedQty());
        vo.setActualQty(line.getActualQty());
        vo.setLocationId(line.getLocationId());
        vo.setAllocationId(line.getAllocationId());
        vo.setRemark(line.getRemark());
        return vo;
    }
}
