package com.jingwei.warehouse.application.service;

import com.jingwei.common.domain.model.UserContext;
import com.jingwei.warehouse.application.dto.ConfirmShipmentDTO;
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

    /**
     * 确认发货
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmShipment(ConfirmShipmentDTO dto) {
        Long operatorId = UserContext.getUserId();
        shipmentDomainService.confirmShipment(dto.getOutboundId(), dto.getSalesOrderId(), operatorId);
    }
}
