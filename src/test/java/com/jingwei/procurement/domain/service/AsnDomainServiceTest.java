package com.jingwei.procurement.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.procurement.domain.model.*;
import com.jingwei.procurement.domain.repository.AsnLineRepository;
import com.jingwei.procurement.domain.repository.AsnRepository;
import com.jingwei.procurement.domain.repository.ProcurementOrderLineRepository;
import com.jingwei.procurement.domain.repository.ProcurementOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 到货通知单领域服务测试
 * <p>
 * 覆盖 T-28 验收标准：
 * <ul>
 *   <li>收货 → 在途库存减少、质检库存增加</li>
 *   <li>部分收货 → 只处理本次实收数量，剩余仍在途</li>
 *   <li>检验PASS → 质检库存转可用库存</li>
 *   <li>检验FAIL → 生成退货出库单，质检库存减少</li>
 *   <li>让步接收CONCESSION → 质检库存转可用库存并标记降级</li>
 *   <li>检验记录完整保存（检验项、结果、检验人、时间）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class AsnDomainServiceTest {

    @Mock
    private AsnRepository asnRepository;
    @Mock
    private AsnLineRepository asnLineRepository;
    @Mock
    private ProcurementOrderRepository procurementOrderRepository;
    @Mock
    private ProcurementOrderLineRepository procurementOrderLineRepository;
    @Mock
    private CodingRuleDomainService codingRuleDomainService;
    @Mock
    private InventoryChangeService inventoryChangeService;

    private AsnDomainService domainService;

    @Captor
    private ArgumentCaptor<AsnLine> asnLineCaptor;
    @Captor
    private ArgumentCaptor<ProcurementOrderLine> orderLineCaptor;

    @BeforeEach
    void setUp() {
        domainService = new AsnDomainService(
                asnRepository, asnLineRepository,
                procurementOrderRepository, procurementOrderLineRepository,
                codingRuleDomainService, inventoryChangeService);
    }

    // ==================== 辅助方法 ====================

    private Asn buildAsn(Long id, AsnStatus status) {
        Asn asn = new Asn();
        asn.setId(id);
        asn.setAsnNo("ASN-202605-00001");
        asn.setProcurementOrderId(1L);
        asn.setSupplierId(1L);
        asn.setStatus(status);
        return asn;
    }

    private AsnLine buildAsnLine(Long id, Long asnId, Long materialId,
                                  BigDecimal expected, BigDecimal received,
                                  QcStatus qcStatus, BigDecimal accepted, BigDecimal rejected) {
        AsnLine line = new AsnLine();
        line.setId(id);
        line.setAsnId(asnId);
        line.setProcurementLineId(10L);
        line.setMaterialId(materialId);
        line.setExpectedQuantity(expected);
        line.setReceivedQuantity(received);
        line.setQcStatus(qcStatus);
        line.setAcceptedQuantity(accepted);
        line.setRejectedQuantity(rejected);
        return line;
    }

    private ProcurementOrderLine buildOrderLine(Long id, BigDecimal quantity, BigDecimal delivered) {
        ProcurementOrderLine line = new ProcurementOrderLine();
        line.setId(id);
        line.setOrderId(1L);
        line.setMaterialId(1L);
        line.setQuantity(quantity);
        line.setDeliveredQuantity(delivered);
        line.setAcceptedQuantity(BigDecimal.ZERO);
        line.setRejectedQuantity(BigDecimal.ZERO);
        return line;
    }

    private QcResult buildQcResult(String inspector, List<QcResult.QcItem> items, String overallResult) {
        QcResult result = new QcResult();
        result.setInspector(inspector);
        result.setInspectedAt(LocalDateTime.now());
        result.setItems(items);
        result.setOverallResult(overallResult);
        result.setConclusion("测试结论");
        return result;
    }

    // ==================== 创建ASN测试 ====================

    @Nested
    @DisplayName("创建到货通知单")
    class CreateAsn {

        @Test
        @DisplayName("创建ASN → 状态为PENDING，自动生成单号")
        void shouldCreateAsnWithPendingStatus() {
            when(codingRuleDomainService.generateCode(any(), any())).thenReturn("ASN-202605-00001");
            when(asnRepository.insert(any())).thenReturn(1);
            when(asnLineRepository.insert(any())).thenReturn(1);

            // 采购订单行剩余可收数量 = 100 - 0 = 100
            ProcurementOrderLine orderLine = buildOrderLine(10L, new BigDecimal("100"), BigDecimal.ZERO);
            when(procurementOrderLineRepository.selectById(10L)).thenReturn(orderLine);

            Asn asn = new Asn();
            asn.setProcurementOrderId(1L);
            asn.setSupplierId(1L);

            AsnLine line = new AsnLine();
            line.setProcurementLineId(10L);
            line.setMaterialId(1L);
            line.setExpectedQuantity(new BigDecimal("50"));
            asn.setLines(List.of(line));

            Asn result = domainService.createAsn(asn);

            assertEquals(AsnStatus.PENDING, result.getStatus());
            assertEquals("ASN-202605-00001", result.getAsnNo());
            assertEquals(QcStatus.PENDING, line.getQcStatus());
            assertEquals(BigDecimal.ZERO, line.getReceivedQuantity());
        }

        @Test
        @DisplayName("到货数量超过剩余可收 → 抛异常")
        void shouldRejectWhenQuantityExceedsRemaining() {
            // 采购订单行：总量100，已到货80，剩余可收20
            ProcurementOrderLine orderLine = buildOrderLine(10L, new BigDecimal("100"), new BigDecimal("80"));
            when(procurementOrderLineRepository.selectById(10L)).thenReturn(orderLine);

            Asn asn = new Asn();
            asn.setProcurementOrderId(1L);
            asn.setSupplierId(1L);

            AsnLine line = new AsnLine();
            line.setProcurementLineId(10L);
            line.setMaterialId(1L);
            line.setExpectedQuantity(new BigDecimal("30")); // 超过剩余20
            asn.setLines(List.of(line));

            assertThrows(BizException.class, () -> domainService.createAsn(asn));
        }
    }

    // ==================== 收货测试 ====================

    @Nested
    @DisplayName("收货确认")
    class ReceiveGoods {

        @Test
        @DisplayName("收货 → 更新实收数量，采购订单行已到货数量增加，触发库存变更")
        void shouldUpdateReceivedQuantityAndTriggerInventoryChange() {
            Asn asn = buildAsn(1L, AsnStatus.PENDING);
            when(asnRepository.selectById(1L)).thenReturn(asn);
            when(asnRepository.updateById(any())).thenReturn(1);

            AsnLine line = buildAsnLine(100L, 1L, 1L,
                    new BigDecimal("50"), BigDecimal.ZERO, QcStatus.PENDING, BigDecimal.ZERO, BigDecimal.ZERO);
            when(asnLineRepository.selectById(100L)).thenReturn(line);
            when(asnLineRepository.updateById(any())).thenReturn(1);

            ProcurementOrderLine orderLine = buildOrderLine(10L, new BigDecimal("100"), BigDecimal.ZERO);
            when(procurementOrderLineRepository.selectById(10L)).thenReturn(orderLine);
            when(procurementOrderLineRepository.updateById(any())).thenReturn(1);

            List<AsnDomainService.ReceiveLine> receiveLines = List.of(
                    new AsnDomainService.ReceiveLine(100L, new BigDecimal("50")));

            domainService.receiveGoods(1L, receiveLines, 200L);

            // 验证ASN行实收数量更新
            verify(asnLineRepository).updateById(asnLineCaptor.capture());
            assertEquals(new BigDecimal("50"), asnLineCaptor.getValue().getReceivedQuantity());

            // 验证采购订单行已到货数量增加
            verify(procurementOrderLineRepository).updateById(orderLineCaptor.capture());
            assertEquals(new BigDecimal("50"), orderLineCaptor.getValue().getDeliveredQuantity());

            // 验证库存变更：在途→质检
            verify(inventoryChangeService).inTransitToQc(eq(1L), eq(new BigDecimal("50")));

            // 验证ASN状态变为RECEIVED
            assertEquals(AsnStatus.RECEIVED, asn.getStatus());
        }

        @Test
        @DisplayName("部分收货 → 只处理本次实收数量，剩余仍在途")
        void shouldHandlePartialReceive() {
            Asn asn = buildAsn(1L, AsnStatus.PENDING);
            when(asnRepository.selectById(1L)).thenReturn(asn);
            when(asnRepository.updateById(any())).thenReturn(1);

            // 预计到货100，本次只收60
            AsnLine line = buildAsnLine(100L, 1L, 1L,
                    new BigDecimal("100"), BigDecimal.ZERO, QcStatus.PENDING, BigDecimal.ZERO, BigDecimal.ZERO);
            when(asnLineRepository.selectById(100L)).thenReturn(line);
            when(asnLineRepository.updateById(any())).thenReturn(1);

            ProcurementOrderLine orderLine = buildOrderLine(10L, new BigDecimal("200"), BigDecimal.ZERO);
            when(procurementOrderLineRepository.selectById(10L)).thenReturn(orderLine);
            when(procurementOrderLineRepository.updateById(any())).thenReturn(1);

            List<AsnDomainService.ReceiveLine> receiveLines = List.of(
                    new AsnDomainService.ReceiveLine(100L, new BigDecimal("60")));

            domainService.receiveGoods(1L, receiveLines, 200L);

            // 验证实收60
            verify(asnLineRepository).updateById(asnLineCaptor.capture());
            assertEquals(new BigDecimal("60"), asnLineCaptor.getValue().getReceivedQuantity());

            // 验证采购订单行已到货60（不是100）
            verify(procurementOrderLineRepository).updateById(orderLineCaptor.capture());
            assertEquals(new BigDecimal("60"), orderLineCaptor.getValue().getDeliveredQuantity());

            // 验证库存变更只处理60
            verify(inventoryChangeService).inTransitToQc(eq(1L), eq(new BigDecimal("60")));
        }

        @Test
        @DisplayName("非法状态收货 → 抛异常")
        void shouldRejectReceiveWhenStatusInvalid() {
            Asn asn = buildAsn(1L, AsnStatus.RECEIVED);
            when(asnRepository.selectById(1L)).thenReturn(asn);

            assertThrows(BizException.class,
                    () -> domainService.receiveGoods(1L, List.of(), 200L));
        }
    }

    // ==================== 检验结果测试 ====================

    @Nested
    @DisplayName("检验结果提交")
    class QcResultSubmission {

        @Test
        @DisplayName("检验PASS → 质检库存转可用库存")
        void shouldTransferQcToAvailableOnPass() {
            // 实收50，合格50，不合格0
            AsnLine line = buildAsnLine(100L, 1L, 1L,
                    new BigDecimal("50"), new BigDecimal("50"), QcStatus.PENDING,
                    new BigDecimal("50"), BigDecimal.ZERO);
            when(asnLineRepository.selectById(100L)).thenReturn(line);
            when(asnLineRepository.updateById(any())).thenReturn(1);

            ProcurementOrderLine orderLine = buildOrderLine(10L, new BigDecimal("100"), new BigDecimal("50"));
            when(procurementOrderLineRepository.selectById(10L)).thenReturn(orderLine);
            when(procurementOrderLineRepository.updateById(any())).thenReturn(1);

            com.jingwei.procurement.domain.model.QcResult qcResult =
                    buildQcResult("张三", List.of(), "PASS");

            domainService.submitQcResult(100L, qcResult);

            // 验证检验状态为PASSED
            verify(asnLineRepository).updateById(asnLineCaptor.capture());
            assertEquals(QcStatus.PASSED, asnLineCaptor.getValue().getQcStatus());

            // 验证质检转可用
            verify(inventoryChangeService).qcToAvailable(eq(1L), eq(new BigDecimal("50")));
            verify(inventoryChangeService, never()).qcOut(any(), any());
        }

        @Test
        @DisplayName("检验FAIL → 质检库存减少（退货）")
        void shouldReduceQcInventoryOnFail() {
            // 实收50，合格0，不合格50
            AsnLine line = buildAsnLine(100L, 1L, 1L,
                    new BigDecimal("50"), new BigDecimal("50"), QcStatus.PENDING,
                    BigDecimal.ZERO, new BigDecimal("50"));
            when(asnLineRepository.selectById(100L)).thenReturn(line);
            when(asnLineRepository.updateById(any())).thenReturn(1);

            ProcurementOrderLine orderLine = buildOrderLine(10L, new BigDecimal("100"), new BigDecimal("50"));
            when(procurementOrderLineRepository.selectById(10L)).thenReturn(orderLine);
            when(procurementOrderLineRepository.updateById(any())).thenReturn(1);

            com.jingwei.procurement.domain.model.QcResult qcResult =
                    buildQcResult("张三", List.of(), "FAIL");

            domainService.submitQcResult(100L, qcResult);

            // 验证检验状态为FAILED
            verify(asnLineRepository).updateById(asnLineCaptor.capture());
            assertEquals(QcStatus.FAILED, asnLineCaptor.getValue().getQcStatus());

            // 验证质检出库（退货）
            verify(inventoryChangeService).qcOut(eq(1L), eq(new BigDecimal("50")));
            verify(inventoryChangeService, never()).qcToAvailable(any(), any());
        }

        @Test
        @DisplayName("让步接收CONCESSION → 质检库存转可用库存并标记降级")
        void shouldHandleConcessionReceive() {
            // 实收100，合格80，不合格20
            AsnLine line = buildAsnLine(100L, 1L, 1L,
                    new BigDecimal("100"), new BigDecimal("100"), QcStatus.PENDING,
                    new BigDecimal("80"), new BigDecimal("20"));
            when(asnLineRepository.selectById(100L)).thenReturn(line);
            when(asnLineRepository.updateById(any())).thenReturn(1);

            ProcurementOrderLine orderLine = buildOrderLine(10L, new BigDecimal("200"), new BigDecimal("100"));
            when(procurementOrderLineRepository.selectById(10L)).thenReturn(orderLine);
            when(procurementOrderLineRepository.updateById(any())).thenReturn(1);

            com.jingwei.procurement.domain.model.QcResult qcResult =
                    buildQcResult("张三", List.of(), "CONCESSION");

            domainService.submitQcResult(100L, qcResult);

            // 验证检验状态为CONCESSION
            verify(asnLineRepository).updateById(asnLineCaptor.capture());
            assertEquals(QcStatus.CONCESSION, asnLineCaptor.getValue().getQcStatus());

            // 验证：合格部分质检转可用 + 不合格部分质检出库
            verify(inventoryChangeService).qcToAvailable(eq(1L), eq(new BigDecimal("80")));
            verify(inventoryChangeService).qcOut(eq(1L), eq(new BigDecimal("20")));
        }

        @Test
        @DisplayName("检验记录完整保存（检验项、结果、检验人、时间）")
        void shouldSaveCompleteQcRecord() {
            AsnLine line = buildAsnLine(100L, 1L, 1L,
                    new BigDecimal("50"), new BigDecimal("50"), QcStatus.PENDING,
                    new BigDecimal("50"), BigDecimal.ZERO);
            when(asnLineRepository.selectById(100L)).thenReturn(line);
            when(asnLineRepository.updateById(any())).thenReturn(1);

            ProcurementOrderLine orderLine = buildOrderLine(10L, new BigDecimal("100"), new BigDecimal("50"));
            when(procurementOrderLineRepository.selectById(10L)).thenReturn(orderLine);
            when(procurementOrderLineRepository.updateById(any())).thenReturn(1);

            // 构建包含检验项的结果
            com.jingwei.procurement.domain.model.QcResult.QcItem item1 = new com.jingwei.procurement.domain.model.QcResult.QcItem();
            item1.setName("色差");
            item1.setStandard("≥4级");
            item1.setActual("4-5级");
            item1.setResult("PASS");

            com.jingwei.procurement.domain.model.QcResult.QcItem item2 = new com.jingwei.procurement.domain.model.QcResult.QcItem();
            item2.setName("克重");
            item2.setStandard("280±10g/m²");
            item2.setActual("278g/m²");
            item2.setResult("PASS");

            com.jingwei.procurement.domain.model.QcResult qcResult =
                    buildQcResult("张三", List.of(item1, item2), "PASS");

            domainService.submitQcResult(100L, qcResult);

            // 验证检验记录完整保存
            verify(asnLineRepository).updateById(asnLineCaptor.capture());
            AsnLine savedLine = asnLineCaptor.getValue();
            assertNotNull(savedLine.getQcResult());
            assertEquals("张三", savedLine.getQcResult().getInspector());
            assertNotNull(savedLine.getQcResult().getInspectedAt());
            assertEquals(2, savedLine.getQcResult().getItems().size());
            assertEquals("色差", savedLine.getQcResult().getItems().get(0).getName());
            assertEquals("PASS", savedLine.getQcResult().getItems().get(0).getResult());
            assertEquals("PASS", savedLine.getQcResult().getOverallResult());
        }

        @Test
        @DisplayName("已检验的行不可重复提交")
        void shouldRejectDuplicateQcSubmission() {
            AsnLine line = buildAsnLine(100L, 1L, 1L,
                    new BigDecimal("50"), new BigDecimal("50"), QcStatus.PASSED,
                    new BigDecimal("50"), BigDecimal.ZERO);
            when(asnLineRepository.selectById(100L)).thenReturn(line);

            com.jingwei.procurement.domain.model.QcResult qcResult =
                    buildQcResult("张三", List.of(), "PASS");

            assertThrows(BizException.class, () -> domainService.submitQcResult(100L, qcResult));
        }
    }
}
