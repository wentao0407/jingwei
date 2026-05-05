package com.jingwei.warehouse.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.inventory.domain.service.InventoryDomainService;
import com.jingwei.master.domain.model.Location;
import com.jingwei.master.domain.model.LocationStatus;
import com.jingwei.master.domain.repository.LocationRepository;
import com.jingwei.procurement.domain.model.Asn;
import com.jingwei.procurement.domain.model.AsnLine;
import com.jingwei.procurement.domain.repository.AsnLineRepository;
import com.jingwei.procurement.domain.repository.AsnRepository;
import com.jingwei.warehouse.domain.model.*;
import com.jingwei.warehouse.domain.repository.ReceivingLineRepository;
import com.jingwei.warehouse.domain.repository.ReceivingOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 收货领域服务单元测试
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class ReceivingDomainServiceTest {

    @Mock private ReceivingOrderRepository receivingOrderRepository;
    @Mock private ReceivingLineRepository receivingLineRepository;
    @Mock private AsnRepository asnRepository;
    @Mock private AsnLineRepository asnLineRepository;
    @Mock private InventoryDomainService inventoryDomainService;
    @Mock private LocationRepository locationRepository;

    private ReceivingDomainService service;

    @BeforeEach
    void setUp() {
        service = new ReceivingDomainService(
                receivingOrderRepository, receivingLineRepository,
                asnRepository, asnLineRepository, inventoryDomainService, locationRepository);
    }

    private AsnLine buildAsnLine(Long id, Long materialId, BigDecimal expected, BigDecimal received) {
        AsnLine line = new AsnLine();
        line.setId(id);
        line.setMaterialId(materialId);
        line.setExpectedQuantity(expected);
        line.setReceivedQuantity(received);
        return line;
    }

    @Nested
    @DisplayName("创建收货单")
    class CreateFromAsnTests {

        @Test
        @DisplayName("正常创建 → 生成收货单和收货行")
        void create_shouldGenerateReceivingOrder() {
            Asn asn = new Asn();
            asn.setId(1L);
            when(asnRepository.selectById(1L)).thenReturn(asn);

            AsnLine asnLine = buildAsnLine(10L, 201L, BigDecimal.valueOf(100), BigDecimal.ZERO);
            when(asnLineRepository.selectByAsnId(1L)).thenReturn(List.of(asnLine));
            when(receivingOrderRepository.insert(any())).thenReturn(1);
            when(receivingLineRepository.batchInsert(any())).thenReturn(1);

            ReceivingOrder result = service.createFromAsn(1L, 1L, 1L);

            assertEquals(ReceivingStatus.IN_PROGRESS, result.getStatus());
            assertEquals(1, result.getLines().size());
            assertEquals(BigDecimal.valueOf(100), result.getLines().get(0).getExpectedQty());
        }

        @Test
        @DisplayName("ASN 不存在 → 抛异常")
        void create_asnNotFound_shouldThrow() {
            when(asnRepository.selectById(999L)).thenReturn(null);
            assertThrows(BizException.class, () -> service.createFromAsn(999L, 1L, 1L));
        }

        @Test
        @DisplayName("ASN 无明细行 → 抛异常")
        void create_emptyAsnLines_shouldThrow() {
            Asn asn = new Asn();
            asn.setId(1L);
            when(asnRepository.selectById(1L)).thenReturn(asn);
            when(asnLineRepository.selectByAsnId(1L)).thenReturn(List.of());

            assertThrows(BizException.class, () -> service.createFromAsn(1L, 1L, 1L));
        }
    }

    @Nested
    @DisplayName("确认收货")
    class ConfirmReceiveTests {

        @Test
        @DisplayName("正常收货 → 更新实收数量")
        void confirm_shouldUpdateReceivedQty() {
            ReceivingLine line = new ReceivingLine();
            line.setId(1L);
            line.setReceivingId(1L);
            line.setMaterialId(201L);
            line.setExpectedQty(BigDecimal.valueOf(100));
            line.setReceivedQty(BigDecimal.ZERO);
            when(receivingLineRepository.selectById(1L)).thenReturn(line);
            when(receivingLineRepository.updateById(any())).thenReturn(1);

            ReceivingOrder order = new ReceivingOrder();
            order.setId(1L);
            order.setWarehouseId(1L);
            when(receivingOrderRepository.selectById(1L)).thenReturn(order);

            service.confirmReceive(1L, BigDecimal.valueOf(60), 3, 1L);

            assertEquals(BigDecimal.valueOf(60), line.getReceivedQty());
            assertEquals(BigDecimal.valueOf(-40), line.getDifferenceQty());
        }

        @Test
        @DisplayName("实收超过可收 → 抛异常")
        void confirm_exceedExpected_shouldThrow() {
            ReceivingLine line = new ReceivingLine();
            line.setId(1L);
            line.setExpectedQty(BigDecimal.valueOf(100));
            line.setReceivedQty(BigDecimal.valueOf(80));
            when(receivingLineRepository.selectById(1L)).thenReturn(line);

            assertThrows(BizException.class, () ->
                    service.confirmReceive(1L, BigDecimal.valueOf(30), null, 1L));
        }
    }

    @Nested
    @DisplayName("确认上架")
    class ConfirmPutawayTests {

        @Test
        @DisplayName("正常上架 → 更新库位和上架状态")
        void putaway_shouldUpdateLocationAndStatus() {
            ReceivingLine line = new ReceivingLine();
            line.setId(1L);
            line.setReceivingId(1L);
            line.setReceivedQty(BigDecimal.valueOf(60));
            when(receivingLineRepository.selectById(1L)).thenReturn(line);
            when(receivingLineRepository.updateById(any())).thenReturn(1);

            Location location = new Location();
            location.setId(10L);
            location.setStatus(LocationStatus.ACTIVE);
            location.setCapacity(500);
            location.setUsedCapacity(100);
            when(locationRepository.selectById(10L)).thenReturn(location);
            when(locationRepository.updateById(any())).thenReturn(1);

            service.confirmPutaway(1L, 10L, 1L);

            assertEquals(PutawayLineStatus.COMPLETED, line.getPutawayStatus());
            assertEquals(10L, line.getPutawayLocationId());
            assertEquals(160, location.getUsedCapacity());
        }

        @Test
        @DisplayName("冻结库位 → 抛异常")
        void putaway_frozenLocation_shouldThrow() {
            ReceivingLine line = new ReceivingLine();
            line.setId(1L);
            when(receivingLineRepository.selectById(1L)).thenReturn(line);

            Location location = new Location();
            location.setId(10L);
            location.setStatus(LocationStatus.FROZEN);
            when(locationRepository.selectById(10L)).thenReturn(location);

            assertThrows(BizException.class, () ->
                    service.confirmPutaway(1L, 10L, 1L));
        }
    }

    @Nested
    @DisplayName("推荐库位")
    class SuggestLocationsTests {

        @Test
        @DisplayName("推荐 → 只返回 ACTIVE 状态库位")
        void suggest_shouldFilterActiveOnly() {
            ReceivingLine line = new ReceivingLine();
            line.setId(1L);
            line.setReceivingId(1L);
            when(receivingLineRepository.selectById(1L)).thenReturn(line);

            ReceivingOrder order = new ReceivingOrder();
            order.setWarehouseId(1L);
            when(receivingOrderRepository.selectById(1L)).thenReturn(order);

            Location active = new Location();
            active.setId(1L);
            active.setStatus(LocationStatus.ACTIVE);
            active.setLocationType(com.jingwei.master.domain.model.LocationType.STORAGE);
            active.setUsedCapacity(10);
            Location frozen = new Location();
            frozen.setId(2L);
            frozen.setStatus(LocationStatus.FROZEN);
            frozen.setLocationType(com.jingwei.master.domain.model.LocationType.STORAGE);
            when(locationRepository.selectByWarehouseId(1L)).thenReturn(List.of(active, frozen));

            var result = service.suggestLocations(1L);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
        }
    }
}
