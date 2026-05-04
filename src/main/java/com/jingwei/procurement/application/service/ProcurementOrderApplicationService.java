package com.jingwei.procurement.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.UserContext;
import com.jingwei.master.domain.model.Material;
import com.jingwei.master.domain.model.Supplier;
import com.jingwei.master.domain.repository.MaterialRepository;
import com.jingwei.master.domain.repository.SupplierRepository;
import com.jingwei.procurement.application.dto.CreateProcurementOrderDTO;
import com.jingwei.procurement.application.dto.FireProcurementOrderEventDTO;
import com.jingwei.procurement.application.dto.ProcurementOrderQueryDTO;
import com.jingwei.procurement.domain.model.ProcurementOrder;
import com.jingwei.procurement.domain.model.ProcurementOrderEvent;
import com.jingwei.procurement.domain.model.ProcurementOrderLine;
import com.jingwei.procurement.domain.model.ProcurementOrderStatus;
import com.jingwei.procurement.domain.repository.ProcurementOrderRepository;
import com.jingwei.procurement.domain.service.ProcurementOrderDomainService;
import com.jingwei.procurement.interfaces.vo.ProcurementOrderLineVO;
import com.jingwei.procurement.interfaces.vo.ProcurementOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 采购订单应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcurementOrderApplicationService {

    private final ProcurementOrderDomainService procurementOrderDomainService;
    private final ProcurementOrderRepository procurementOrderRepository;
    private final SupplierRepository supplierRepository;
    private final MaterialRepository materialRepository;

    /**
     * 创建采购订单
     */
    @Transactional(rollbackFor = Exception.class)
    public ProcurementOrderVO createOrder(CreateProcurementOrderDTO dto) {
        ProcurementOrder order = new ProcurementOrder();
        order.setSupplierId(dto.getSupplierId());
        order.setOrderDate(dto.getOrderDate() != null ? LocalDate.parse(dto.getOrderDate()) : LocalDate.now());
        order.setExpectedDeliveryDate(dto.getExpectedDeliveryDate() != null
                ? LocalDate.parse(dto.getExpectedDeliveryDate()) : null);
        order.setRemark(dto.getRemark());

        List<ProcurementOrderLine> lines = buildOrderLines(dto.getLines());

        ProcurementOrder saved = procurementOrderDomainService.createOrder(order, lines);
        return toProcurementOrderVO(saved);
    }

    /**
     * 触发状态转移
     */
    @Transactional(rollbackFor = Exception.class)
    public void fireEvent(FireProcurementOrderEventDTO dto) {
        Long operatorId = UserContext.getUserId();
        ProcurementOrderEvent event = ProcurementOrderEvent.valueOf(dto.getEvent());
        procurementOrderDomainService.fireEvent(dto.getOrderId(), event, operatorId);
    }

    /**
     * 查询详情
     */
    public ProcurementOrderVO getDetail(Long orderId) {
        ProcurementOrder order = procurementOrderDomainService.getOrderDetail(orderId);
        return toProcurementOrderVO(order);
    }

    /**
     * 分页查询
     */
    public IPage<ProcurementOrderVO> pageQuery(ProcurementOrderQueryDTO dto) {
        Page<ProcurementOrder> page = new Page<>(dto.getCurrent(), dto.getSize());
        ProcurementOrderStatus status = dto.getStatus() != null
                ? ProcurementOrderStatus.valueOf(dto.getStatus()) : null;

        IPage<ProcurementOrder> orderPage = procurementOrderRepository.selectPage(
                page, dto.getSupplierId(), status);
        return orderPage.convert(this::toProcurementOrderVO);
    }

    /**
     * 获取可用操作
     */
    public List<String> getAvailableActions(Long orderId) {
        return procurementOrderDomainService.getAvailableActions(orderId);
    }

    // ==================== 私有方法 ====================

    private List<ProcurementOrderLine> buildOrderLines(
            List<CreateProcurementOrderDTO.ProcurementOrderLineCreateDTO> lineDTOs) {
        List<ProcurementOrderLine> lines = new ArrayList<>();
        for (CreateProcurementOrderDTO.ProcurementOrderLineCreateDTO dto : lineDTOs) {
            ProcurementOrderLine line = new ProcurementOrderLine();
            line.setMaterialId(dto.getMaterialId());
            line.setMaterialType(dto.getMaterialType());
            line.setQuantity(dto.getQuantity());
            line.setUnit(dto.getUnit());
            line.setUnitPrice(dto.getUnitPrice());
            line.setMrpResultId(dto.getMrpResultId());
            line.setRemark(dto.getRemark());
            lines.add(line);
        }
        return lines;
    }

    private ProcurementOrderVO toProcurementOrderVO(ProcurementOrder order) {
        ProcurementOrderVO vo = new ProcurementOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setSupplierId(order.getSupplierId());
        vo.setOrderDate(order.getOrderDate() != null ? order.getOrderDate().toString() : null);
        vo.setExpectedDeliveryDate(order.getExpectedDeliveryDate() != null
                ? order.getExpectedDeliveryDate().toString() : null);
        vo.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        vo.setStatusLabel(order.getStatus() != null ? order.getStatus().getLabel() : null);
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPaidAmount(order.getPaidAmount());
        vo.setPaymentStatus(order.getPaymentStatus());
        vo.setMrpBatchNo(order.getMrpBatchNo());
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());

        // 补充供应商名称
        if (order.getSupplierId() != null) {
            Supplier supplier = supplierRepository.selectById(order.getSupplierId());
            if (supplier != null) {
                vo.setSupplierName(supplier.getName());
            }
        }

        if (order.getLines() != null && !order.getLines().isEmpty()) {
            vo.setLines(order.getLines().stream().map(this::toLineVO).toList());
        } else {
            vo.setLines(List.of());
        }

        return vo;
    }

    private ProcurementOrderLineVO toLineVO(ProcurementOrderLine line) {
        ProcurementOrderLineVO vo = new ProcurementOrderLineVO();
        vo.setId(line.getId());
        vo.setLineNo(line.getLineNo());
        vo.setMaterialId(line.getMaterialId());
        vo.setMaterialType(line.getMaterialType());
        vo.setQuantity(line.getQuantity());
        vo.setUnit(line.getUnit());
        vo.setUnitPrice(line.getUnitPrice());
        vo.setLineAmount(line.getLineAmount());
        vo.setDeliveredQuantity(line.getDeliveredQuantity());
        vo.setAcceptedQuantity(line.getAcceptedQuantity());
        vo.setRejectedQuantity(line.getRejectedQuantity());
        vo.setMrpResultId(line.getMrpResultId());
        vo.setRemark(line.getRemark());

        if (line.getMaterialId() != null) {
            Material material = materialRepository.selectById(line.getMaterialId());
            if (material != null) {
                vo.setMaterialCode(material.getCode());
                vo.setMaterialName(material.getName());
            }
        }

        return vo;
    }
}
