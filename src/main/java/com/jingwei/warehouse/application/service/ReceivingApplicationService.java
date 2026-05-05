package com.jingwei.warehouse.application.service;

import com.jingwei.common.domain.model.UserContext;
import com.jingwei.master.domain.model.Location;
import com.jingwei.master.domain.model.Material;
import com.jingwei.master.domain.repository.LocationRepository;
import com.jingwei.master.domain.repository.MaterialRepository;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.warehouse.application.dto.ConfirmPutawayDTO;
import com.jingwei.warehouse.application.dto.ConfirmReceiveDTO;
import com.jingwei.warehouse.application.dto.CreateReceivingDTO;
import com.jingwei.warehouse.domain.model.ReceivingLine;
import com.jingwei.warehouse.domain.model.ReceivingOrder;
import com.jingwei.warehouse.domain.service.ReceivingDomainService;
import com.jingwei.warehouse.domain.repository.ReceivingOrderRepository;
import com.jingwei.warehouse.interfaces.vo.ReceivingLineVO;
import com.jingwei.warehouse.interfaces.vo.ReceivingOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 收货应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceivingApplicationService {

    /** 编码规则键：收货单号 */
    private static final String RECEIVING_NO_RULE = "RECEIVING_NO";

    private final ReceivingDomainService receivingDomainService;
    private final ReceivingOrderRepository receivingOrderRepository;
    private final CodingRuleDomainService codingRuleDomainService;
    private final MaterialRepository materialRepository;
    private final LocationRepository locationRepository;

    /**
     * 从 ASN 创建收货单
     */
    @Transactional(rollbackFor = Exception.class)
    public ReceivingOrderVO createFromAsn(CreateReceivingDTO dto) {
        Long operatorId = UserContext.getUserId();

        // 生成收货单号
        String receivingNo = codingRuleDomainService.generateCode(
                RECEIVING_NO_RULE, java.util.Collections.emptyMap());

        ReceivingOrder order = receivingDomainService.createFromAsn(
                dto.getAsnId(), dto.getWarehouseId(), operatorId);
        order.setReceivingNo(receivingNo);
        order.setDockNo(dto.getDockNo());
        receivingOrderRepository.updateById(order);

        return toVO(order);
    }

    /**
     * 确认收货
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(ConfirmReceiveDTO dto) {
        Long operatorId = UserContext.getUserId();
        receivingDomainService.confirmReceive(
                dto.getReceivingLineId(), dto.getReceivedQty(), dto.getRollCount(), operatorId);
    }

    /**
     * 推荐上架库位
     */
    public List<Map<String, Object>> suggestLocations(Long receivingLineId) {
        List<Location> locations = receivingDomainService.suggestLocations(receivingLineId);
        return locations.stream().map(loc -> {
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("locationId", loc.getId());
            map.put("fullCode", loc.getFullCode());
            map.put("locationType", loc.getLocationType() != null ? loc.getLocationType().name() : null);
            map.put("capacity", loc.getCapacity());
            map.put("usedCapacity", loc.getUsedCapacity());
            int remaining = (loc.getCapacity() != null ? loc.getCapacity() : 0)
                    - (loc.getUsedCapacity() != null ? loc.getUsedCapacity() : 0);
            map.put("remainingCapacity", remaining);
            return (Map<String, Object>) map;
        }).toList();
    }

    /**
     * 确认上架
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmPutaway(ConfirmPutawayDTO dto) {
        Long operatorId = UserContext.getUserId();
        receivingDomainService.confirmPutaway(dto.getReceivingLineId(), dto.getLocationId(), operatorId);
    }

    /**
     * 查询收货单详情
     */
    public ReceivingOrderVO getDetail(Long receivingId) {
        ReceivingOrder order = receivingOrderRepository.selectDetailById(receivingId);
        return toVO(order);
    }

    // ==================== 私有方法 ====================

    private ReceivingOrderVO toVO(ReceivingOrder order) {
        if (order == null) return null;
        ReceivingOrderVO vo = new ReceivingOrderVO();
        vo.setId(order.getId());
        vo.setReceivingNo(order.getReceivingNo());
        vo.setAsnId(order.getAsnId());
        vo.setWarehouseId(order.getWarehouseId());
        vo.setReceivingDate(order.getReceivingDate() != null ? order.getReceivingDate().toString() : null);
        vo.setStatus(order.getStatus() != null ? order.getStatus().getCode() : null);
        vo.setStatusLabel(order.getStatus() != null ? order.getStatus().getLabel() : null);
        vo.setReceiverId(order.getReceiverId());
        vo.setDockNo(order.getDockNo());
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());

        if (order.getLines() != null) {
            vo.setLines(order.getLines().stream().map(this::toLineVO).toList());
        } else {
            vo.setLines(List.of());
        }
        return vo;
    }

    private ReceivingLineVO toLineVO(ReceivingLine line) {
        ReceivingLineVO vo = new ReceivingLineVO();
        vo.setId(line.getId());
        vo.setAsnLineId(line.getAsnLineId());
        vo.setMaterialId(line.getMaterialId());
        vo.setExpectedQty(line.getExpectedQty());
        vo.setReceivedQty(line.getReceivedQty());
        vo.setRollCount(line.getRollCount());
        vo.setDifferenceQty(line.getDifferenceQty());
        vo.setDifferenceReason(line.getDifferenceReason());
        vo.setBatchNo(line.getBatchNo());
        vo.setQcStatus(line.getQcStatus() != null ? line.getQcStatus().getCode() : null);
        vo.setQcStatusLabel(line.getQcStatus() != null ? line.getQcStatus().getLabel() : null);
        vo.setPutawayStatus(line.getPutawayStatus() != null ? line.getPutawayStatus().getCode() : null);
        vo.setPutawayStatusLabel(line.getPutawayStatus() != null ? line.getPutawayStatus().getLabel() : null);
        vo.setPutawayLocationId(line.getPutawayLocationId());
        vo.setRemark(line.getRemark());

        // 补充物料名称
        if (line.getMaterialId() != null) {
            Material mat = materialRepository.selectById(line.getMaterialId());
            if (mat != null) vo.setMaterialName(mat.getName());
        }
        // 补充库位编码
        if (line.getPutawayLocationId() != null) {
            Location loc = locationRepository.selectById(line.getPutawayLocationId());
            if (loc != null) vo.setPutawayLocationCode(loc.getFullCode());
        }

        return vo;
    }
}
