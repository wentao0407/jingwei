package com.jingwei.warehouse.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.inventory.domain.model.OutboundOrder;
import com.jingwei.inventory.domain.model.OutboundOrderLine;
import com.jingwei.inventory.domain.model.OutboundStatus;
import com.jingwei.inventory.domain.repository.OutboundOrderLineRepository;
import com.jingwei.inventory.domain.repository.OutboundOrderRepository;
import com.jingwei.warehouse.domain.model.*;
import com.jingwei.warehouse.domain.repository.PickItemRepository;
import com.jingwei.warehouse.domain.repository.PickListRepository;
import com.jingwei.warehouse.domain.repository.WaveRepository;
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
 * 波次领域服务单元测试
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class WaveDomainServiceTest {

    @Mock private WaveRepository waveRepository;
    @Mock private PickListRepository pickListRepository;
    @Mock private PickItemRepository pickItemRepository;
    @Mock private OutboundOrderRepository outboundOrderRepository;
    @Mock private OutboundOrderLineRepository outboundOrderLineRepository;

    private WaveDomainService service;

    @BeforeEach
    void setUp() {
        service = new WaveDomainService(
                waveRepository, pickListRepository, pickItemRepository,
                outboundOrderRepository, outboundOrderLineRepository);
    }

    @Nested
    @DisplayName("创建波次")
    class CreateWaveTests {

        @Test
        @DisplayName("正常创建 → 生成草稿波次（不含拣货单）")
        void create_shouldGenerateDraftWave() {
            Wave wave = new Wave();
            wave.setWaveNo("WV-20260505-0001");
            wave.setWarehouseId(1L);
            wave.setStrategy(WaveStrategy.BY_CUSTOMER);
            when(waveRepository.insert(any())).thenReturn(1);

            OutboundOrder outbound = new OutboundOrder();
            outbound.setId(1L);
            outbound.setStatus(OutboundStatus.CONFIRMED);
            outbound.setOutboundNo("OB-001");
            OutboundOrderLine line = new OutboundOrderLine();
            line.setId(10L);
            line.setSkuId(100L);
            line.setPlannedQty(BigDecimal.valueOf(30));
            outbound.setLines(List.of(line));
            when(outboundOrderRepository.selectDetailById(1L)).thenReturn(outbound);

            Wave result = service.createWave(wave, List.of(1L));

            assertEquals(WaveStatus.DRAFT, result.getStatus());
            // createWave 不再生成拣货单，需通过 releaseWave 生成
            verify(pickListRepository, never()).insert(any());
        }

        @Test
        @DisplayName("空出库单列表 → 抛异常")
        void create_emptyOutboundIds_shouldThrow() {
            Wave wave = new Wave();
            assertThrows(BizException.class, () -> service.createWave(wave, List.of()));
        }

        @Test
        @DisplayName("出库单不存在 → 抛异常")
        void create_outboundNotFound_shouldThrow() {
            Wave wave = new Wave();
            lenient().when(waveRepository.insert(any())).thenReturn(1);
            when(outboundOrderRepository.selectDetailById(999L)).thenReturn(null);

            assertThrows(BizException.class, () -> service.createWave(wave, List.of(999L)));
        }
    }

    @Nested
    @DisplayName("确认拣货")
    class ConfirmPickTests {

        @Test
        @DisplayName("正常拣货 → 状态变为 COMPLETED")
        void confirm_shouldSetCompleted() {
            PickItem item = new PickItem();
            item.setId(1L);
            item.setPlannedQty(BigDecimal.valueOf(30));
            item.setStatus(PickItemStatus.PICKING);
            when(pickItemRepository.selectById(1L)).thenReturn(item);
            when(pickItemRepository.updateById(any())).thenReturn(1);

            service.confirmPick(1L, BigDecimal.valueOf(30), 1L);

            assertEquals(PickItemStatus.COMPLETED, item.getStatus());
            assertEquals(BigDecimal.valueOf(30), item.getActualQty());
        }

        @Test
        @DisplayName("短拣 → 状态变为 SHORT")
        void confirm_shortPick_shouldSetShort() {
            PickItem item = new PickItem();
            item.setId(1L);
            item.setPlannedQty(BigDecimal.valueOf(30));
            item.setStatus(PickItemStatus.PICKING);
            when(pickItemRepository.selectById(1L)).thenReturn(item);
            when(pickItemRepository.updateById(any())).thenReturn(1);

            service.confirmPick(1L, BigDecimal.valueOf(20), 1L);

            assertEquals(PickItemStatus.SHORT, item.getStatus());
        }
    }

    @Nested
    @DisplayName("完成拣货单")
    class CompletePickListTests {

        @Test
        @DisplayName("全部完成 → 状态 COMPLETED")
        void complete_allCompleted_shouldSetCompleted() {
            PickList pickList = new PickList();
            pickList.setId(1L);
            pickList.setStatus(PickListStatus.PICKING);
            when(pickListRepository.selectById(1L)).thenReturn(pickList);

            PickItem item1 = new PickItem();
            item1.setStatus(PickItemStatus.COMPLETED);
            when(pickItemRepository.selectByPickListId(1L)).thenReturn(List.of(item1));
            when(pickListRepository.updateById(any())).thenReturn(1);

            service.completePickList(1L, 1L);

            assertEquals(PickListStatus.COMPLETED, pickList.getStatus());
        }

        @Test
        @DisplayName("有短拣 → 状态 DISCREPANCY")
        void complete_withShort_shouldSetDiscrepancy() {
            PickList pickList = new PickList();
            pickList.setId(1L);
            when(pickListRepository.selectById(1L)).thenReturn(pickList);

            PickItem item1 = new PickItem();
            item1.setStatus(PickItemStatus.COMPLETED);
            PickItem item2 = new PickItem();
            item2.setStatus(PickItemStatus.SHORT);
            when(pickItemRepository.selectByPickListId(1L)).thenReturn(List.of(item1, item2));
            when(pickListRepository.updateById(any())).thenReturn(1);

            service.completePickList(1L, 1L);

            assertEquals(PickListStatus.DISCREPANCY, pickList.getStatus());
        }

        @Test
        @DisplayName("有未拣货项 → 抛异常")
        void complete_withPending_shouldThrow() {
            PickList pickList = new PickList();
            pickList.setId(1L);
            when(pickListRepository.selectById(1L)).thenReturn(pickList);

            PickItem item1 = new PickItem();
            item1.setStatus(PickItemStatus.PICKING);
            when(pickItemRepository.selectByPickListId(1L)).thenReturn(List.of(item1));

            assertThrows(BizException.class, () -> service.completePickList(1L, 1L));
        }
    }
}
