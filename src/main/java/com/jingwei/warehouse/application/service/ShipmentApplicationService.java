package com.jingwei.warehouse.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.domain.model.UserContext;
import com.jingwei.inventory.application.dto.OutboundQueryDTO;
import com.jingwei.inventory.application.service.OutboundApplicationService;
import com.jingwei.inventory.interfaces.vo.OutboundOrderVO;
import com.jingwei.warehouse.application.dto.ConfirmShipmentDTO;
import com.jingwei.warehouse.application.dto.ShipmentQueryDTO;
import com.jingwei.warehouse.domain.service.ShipmentDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 发货应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentApplicationService {

    private final ShipmentDomainService shipmentDomainService;
    private final OutboundApplicationService outboundApplicationService;

    /**
     * 确认发货
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmShipment(ConfirmShipmentDTO dto) {
        Long operatorId = UserContext.getUserId();
        shipmentDomainService.confirmShipment(dto.getOutboundId(), dto.getSalesOrderId(), operatorId);
    }

    /**
     * 分页查询发运单聚合。
     *
     * 发运单当前复用出库单作为业务聚合根，按仓库、状态和出库单号查询。
     */
    public IPage<OutboundOrderVO> pageShipments(ShipmentQueryDTO dto) {
        OutboundQueryDTO query = new OutboundQueryDTO();
        query.setCurrent(dto.getCurrent());
        query.setSize(dto.getSize());
        query.setStatus(trimToNull(dto.getStatus()));
        query.setWarehouseId(dto.getWarehouseId());
        query.setOutboundNo(trimToNull(dto.getOutboundNo()));
        return outboundApplicationService.pageQuery(query);
    }

    /**
     * 查询发运单详情。
     */
    public OutboundOrderVO getShipmentDetail(Long shipmentId) {
        return outboundApplicationService.getDetail(shipmentId);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
