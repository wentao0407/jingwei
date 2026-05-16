package com.jingwei.warehouse.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.inventory.application.service.OutboundApplicationService;
import com.jingwei.inventory.interfaces.vo.OutboundOrderVO;
import com.jingwei.warehouse.application.dto.ShipmentQueryDTO;
import com.jingwei.warehouse.domain.service.ShipmentDomainService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipmentApplicationServiceTest {

    private final ShipmentDomainService shipmentDomainService = null;
    private final FakeOutboundApplicationService outboundApplicationService = new FakeOutboundApplicationService();
    private final ShipmentApplicationService service = new ShipmentApplicationService(
            shipmentDomainService,
            outboundApplicationService);

    @Test
    void pageShipments_delegatesToOutboundAggregateQuery() {
        OutboundOrderVO outbound = new OutboundOrderVO();
        outbound.setId(80001L);
        outbound.setOutboundNo("CK-202605-0001");
        Page<OutboundOrderVO> page = new Page<>(1, 20, 1);
        page.setRecords(List.of(outbound));
        outboundApplicationService.pageResult = page;

        ShipmentQueryDTO query = new ShipmentQueryDTO();
        query.setCurrent(1L);
        query.setSize(20L);
        query.setStatus("SHIPPED");
        query.setWarehouseId(30001L);
        query.setOutboundNo("CK-202605");

        assertEquals("CK-202605-0001", service.pageShipments(query).getRecords().get(0).getOutboundNo());
        assertEquals(1L, outboundApplicationService.lastQuery.getCurrent());
        assertEquals(20L, outboundApplicationService.lastQuery.getSize());
        assertEquals("SHIPPED", outboundApplicationService.lastQuery.getStatus());
        assertEquals(30001L, outboundApplicationService.lastQuery.getWarehouseId());
        assertEquals("CK-202605", outboundApplicationService.lastQuery.getOutboundNo());
    }

    @Test
    void getShipmentDetail_delegatesToOutboundDetail() {
        OutboundOrderVO outbound = new OutboundOrderVO();
        outbound.setId(80001L);
        outboundApplicationService.detailResult = outbound;

        assertEquals(80001L, service.getShipmentDetail(80001L).getId());
        assertEquals(80001L, outboundApplicationService.lastDetailId);
    }

    private static class FakeOutboundApplicationService extends OutboundApplicationService {
        private OutboundOrderVO detailResult;
        private Long lastDetailId;
        private Page<OutboundOrderVO> pageResult;
        private com.jingwei.inventory.application.dto.OutboundQueryDTO lastQuery;

        FakeOutboundApplicationService() {
            super(null, null, null);
        }

        @Override
        public OutboundOrderVO getDetail(Long outboundId) {
            lastDetailId = outboundId;
            return detailResult;
        }

        @Override
        public Page<OutboundOrderVO> pageQuery(com.jingwei.inventory.application.dto.OutboundQueryDTO dto) {
            lastQuery = dto;
            return pageResult;
        }
    }
}
