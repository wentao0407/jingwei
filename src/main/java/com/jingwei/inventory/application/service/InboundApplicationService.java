package com.jingwei.inventory.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.UserContext;
import com.jingwei.inventory.application.dto.CreateInboundDTO;
import com.jingwei.inventory.application.dto.CreateInboundLineDTO;
import com.jingwei.inventory.application.dto.InboundQueryDTO;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.service.InboundDomainService;
import com.jingwei.inventory.domain.repository.InboundOrderRepository;
import com.jingwei.inventory.interfaces.vo.InboundOrderLineVO;
import com.jingwei.inventory.interfaces.vo.InboundOrderVO;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 入库单应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboundApplicationService {

    /** 编码规则键：入库单编号 */
    private static final String INBOUND_NO_RULE = "INBOUND_NO";

    private final InboundDomainService inboundDomainService;
    private final InboundOrderRepository inboundOrderRepository;
    private final CodingRuleDomainService codingRuleDomainService;

    /**
     * 创建入库单
     */
    @Transactional(rollbackFor = Exception.class)
    public InboundOrderVO createInbound(CreateInboundDTO dto) {
        String inboundNo = codingRuleDomainService.generateCode(
                INBOUND_NO_RULE, java.util.Collections.emptyMap());

        InboundOrder order = new InboundOrder();
        order.setInboundNo(inboundNo);
        order.setInboundType(InboundType.valueOf(dto.getInboundType()));
        order.setWarehouseId(dto.getWarehouseId());
        order.setSourceType(dto.getSourceType());
        order.setSourceId(dto.getSourceId());
        order.setSourceNo(dto.getSourceNo());
        order.setRemark(dto.getRemark());

        List<InboundOrderLine> lines = buildLines(dto.getLines());
        InboundOrder saved = inboundDomainService.createOrder(order, lines);
        return toVO(saved);
    }

    /**
     * 确认入库
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmInbound(Long inboundId) {
        Long operatorId = UserContext.getUserId();
        inboundDomainService.confirmInbound(inboundId, operatorId);
    }

    /**
     * 查询入库单详情
     */
    public InboundOrderVO getDetail(Long inboundId) {
        InboundOrder order = inboundOrderRepository.selectDetailById(inboundId);
        return toVO(order);
    }

    /**
     * 分页查询
     */
    public IPage<InboundOrderVO> pageQuery(InboundQueryDTO dto) {
        Page<InboundOrder> page = new Page<>(dto.getCurrent(), dto.getSize());
        InboundStatus status = dto.getStatus() != null ? InboundStatus.valueOf(dto.getStatus()) : null;
        IPage<InboundOrder> result = inboundOrderRepository.selectPage(
                page, status, dto.getWarehouseId(), dto.getInboundNo());
        return result.convert(this::toVO);
    }

    // ==================== 私有方法 ====================

    private List<InboundOrderLine> buildLines(List<CreateInboundLineDTO> dtos) {
        List<InboundOrderLine> lines = new ArrayList<>();
        for (CreateInboundLineDTO dto : dtos) {
            InboundOrderLine line = new InboundOrderLine();
            line.setInventoryType(InventoryType.valueOf(dto.getInventoryType()));
            line.setSkuId(dto.getSkuId());
            line.setMaterialId(dto.getMaterialId());
            line.setBatchNo(dto.getBatchNo() != null ? dto.getBatchNo() : "");
            line.setPlannedQty(dto.getPlannedQty());
            line.setActualQty(dto.getActualQty());
            line.setLocationId(dto.getLocationId());
            line.setUnitCost(dto.getUnitCost() != null ? dto.getUnitCost() : BigDecimal.ZERO);
            line.setRemark(dto.getRemark());
            lines.add(line);
        }
        return lines;
    }

    private InboundOrderVO toVO(InboundOrder order) {
        if (order == null) return null;
        InboundOrderVO vo = new InboundOrderVO();
        vo.setId(order.getId());
        vo.setInboundNo(order.getInboundNo());
        vo.setInboundType(order.getInboundType() != null ? order.getInboundType().getCode() : null);
        vo.setInboundTypeLabel(order.getInboundType() != null ? order.getInboundType().getLabel() : null);
        vo.setWarehouseId(order.getWarehouseId());
        vo.setStatus(order.getStatus() != null ? order.getStatus().getCode() : null);
        vo.setStatusLabel(order.getStatus() != null ? order.getStatus().getLabel() : null);
        vo.setSourceType(order.getSourceType());
        vo.setSourceId(order.getSourceId());
        vo.setSourceNo(order.getSourceNo());
        vo.setInboundDate(order.getInboundDate() != null ? order.getInboundDate().toString() : null);
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

    private InboundOrderLineVO toLineVO(InboundOrderLine line) {
        InboundOrderLineVO vo = new InboundOrderLineVO();
        vo.setId(line.getId());
        vo.setLineNo(line.getLineNo());
        vo.setInventoryType(line.getInventoryType() != null ? line.getInventoryType().getCode() : null);
        vo.setSkuId(line.getSkuId());
        vo.setMaterialId(line.getMaterialId());
        vo.setBatchNo(line.getBatchNo());
        vo.setPlannedQty(line.getPlannedQty());
        vo.setActualQty(line.getActualQty());
        vo.setLocationId(line.getLocationId());
        vo.setUnitCost(line.getUnitCost());
        vo.setRemark(line.getRemark());
        return vo;
    }
}
