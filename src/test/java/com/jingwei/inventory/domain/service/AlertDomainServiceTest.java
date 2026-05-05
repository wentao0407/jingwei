package com.jingwei.inventory.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.repository.AlertRuleRepository;
import com.jingwei.inventory.domain.repository.InventoryAlertRepository;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 库存预警领域服务单元测试
 *
 * @author JingWei
 */
@ExtendWith(MockitoExtension.class)
class AlertDomainServiceTest {

    @Mock private AlertRuleRepository alertRuleRepository;
    @Mock private InventoryAlertRepository inventoryAlertRepository;
    @Mock private InventorySkuRepository inventorySkuRepository;

    private AlertDomainService service;

    @BeforeEach
    void setUp() {
        service = new AlertDomainService(alertRuleRepository, inventoryAlertRepository, inventorySkuRepository);
    }

    private AlertRule buildRule(Long id, AlertType type, int threshold) {
        AlertRule rule = new AlertRule();
        rule.setId(id);
        rule.setRuleCode("RULE_" + id);
        rule.setRuleName("测试规则" + id);
        rule.setAlertType(type);
        rule.setConditionType(ConditionType.FIXED_VALUE);
        rule.setThresholdValue(BigDecimal.valueOf(threshold));
        rule.setEnabled(true);
        return rule;
    }

    private InventorySku buildSku(Long id, int available, LocalDate lastInbound) {
        InventorySku sku = new InventorySku();
        sku.setId(id);
        sku.setSkuId(100L + id);
        sku.setWarehouseId(1L);
        sku.setAvailableQty(available);
        sku.setLockedQty(0);
        sku.setQcQty(0);
        sku.setTotalQty(available);
        sku.setLastInboundDate(lastInbound);
        return sku;
    }

    @Nested
    @DisplayName("低库存预警")
    class LowStockTests {

        @Test
        @DisplayName("可用库存低于阈值 → 生成 LOW_STOCK 预警")
        void lowStock_shouldGenerateAlert() {
            AlertRule rule = buildRule(1L, AlertType.LOW_STOCK, 10);
            when(alertRuleRepository.selectAllEnabled()).thenReturn(List.of(rule));

            InventorySku sku = buildSku(1L, 5, null);
            when(inventorySkuRepository.selectAll()).thenReturn(List.of(sku));
            when(inventoryAlertRepository.selectActiveBySkuAndRule(1L, 1L)).thenReturn(null);

            int count = service.scanAndAlert();

            assertEquals(1, count);
            verify(inventoryAlertRepository).insert(argThat(alert ->
                    alert.getAlertType() == AlertType.LOW_STOCK
                            && alert.getCurrentValue().intValue() == 5));
        }

        @Test
        @DisplayName("可用库存等于阈值 → 也生成预警（<=）")
        void lowStock_equalThreshold_shouldGenerate() {
            AlertRule rule = buildRule(1L, AlertType.LOW_STOCK, 10);
            when(alertRuleRepository.selectAllEnabled()).thenReturn(List.of(rule));

            InventorySku sku = buildSku(1L, 10, null);
            when(inventorySkuRepository.selectAll()).thenReturn(List.of(sku));
            when(inventoryAlertRepository.selectActiveBySkuAndRule(1L, 1L)).thenReturn(null);

            int count = service.scanAndAlert();
            assertEquals(1, count);
        }

        @Test
        @DisplayName("同一 SKU 同一规则已有 ACTIVE 预警 → 不重复生成")
        void lowStock_duplicate_shouldSkip() {
            AlertRule rule = buildRule(1L, AlertType.LOW_STOCK, 10);
            when(alertRuleRepository.selectAllEnabled()).thenReturn(List.of(rule));

            InventorySku sku = buildSku(1L, 5, null);
            when(inventorySkuRepository.selectAll()).thenReturn(List.of(sku));

            // 已有活跃预警
            InventoryAlert existing = new InventoryAlert();
            existing.setId(99L);
            when(inventoryAlertRepository.selectActiveBySkuAndRule(1L, 1L)).thenReturn(existing);

            int count = service.scanAndAlert();
            assertEquals(0, count);
            verify(inventoryAlertRepository, never()).insert(any());
        }
    }

    @Nested
    @DisplayName("超储预警")
    class OverstockTests {

        @Test
        @DisplayName("可用库存高于阈值 → 生成 OVERSTOCK 预警")
        void overstock_shouldGenerateAlert() {
            AlertRule rule = buildRule(2L, AlertType.OVERSTOCK, 1000);
            when(alertRuleRepository.selectAllEnabled()).thenReturn(List.of(rule));

            InventorySku sku = buildSku(1L, 1500, null);
            when(inventorySkuRepository.selectAll()).thenReturn(List.of(sku));
            when(inventoryAlertRepository.selectActiveBySkuAndRule(1L, 2L)).thenReturn(null);

            int count = service.scanAndAlert();
            assertEquals(1, count);
            verify(inventoryAlertRepository).insert(argThat(alert ->
                    alert.getAlertType() == AlertType.OVERSTOCK));
        }
    }

    @Nested
    @DisplayName("库龄预警")
    class AgingTests {

        @Test
        @DisplayName("库龄超过阈值 → 生成 AGING 预警")
        void aging_shouldGenerateAlert() {
            AlertRule rule = buildRule(3L, AlertType.AGING, 180);
            when(alertRuleRepository.selectAllEnabled()).thenReturn(List.of(rule));

            // 入库日期在200天前
            InventorySku sku = buildSku(1L, 50, LocalDate.now().minusDays(200));
            when(inventorySkuRepository.selectAll()).thenReturn(List.of(sku));
            when(inventoryAlertRepository.selectActiveBySkuAndRule(1L, 3L)).thenReturn(null);

            int count = service.scanAndAlert();
            assertEquals(1, count);
            verify(inventoryAlertRepository).insert(argThat(alert ->
                    alert.getAlertType() == AlertType.AGING
                            && alert.getCurrentValue().longValue() >= 200));
        }

        @Test
        @DisplayName("无入库日期 → 跳过")
        void aging_noInboundDate_shouldSkip() {
            AlertRule rule = buildRule(3L, AlertType.AGING, 180);
            when(alertRuleRepository.selectAllEnabled()).thenReturn(List.of(rule));

            InventorySku sku = buildSku(1L, 50, null);
            when(inventorySkuRepository.selectAll()).thenReturn(List.of(sku));

            int count = service.scanAndAlert();
            assertEquals(0, count);
        }
    }

    @Nested
    @DisplayName("确认预警")
    class AcknowledgeTests {

        @Test
        @DisplayName("正常确认 → 状态变为 ACKNOWLEDGED")
        void acknowledge_shouldUpdateStatus() {
            InventoryAlert alert = new InventoryAlert();
            alert.setId(1L);
            alert.setStatus(AlertStatus.ACTIVE);
            when(inventoryAlertRepository.selectById(1L)).thenReturn(alert);
            when(inventoryAlertRepository.updateById(any())).thenReturn(1);

            service.acknowledge(1L, 1L);

            assertEquals(AlertStatus.ACKNOWLEDGED, alert.getStatus());
            assertEquals(1L, alert.getAcknowledgedBy());
            assertNotNull(alert.getAcknowledgedAt());
        }

        @Test
        @DisplayName("非 ACTIVE 状态确认 → 抛异常")
        void acknowledge_nonActive_shouldThrow() {
            InventoryAlert alert = new InventoryAlert();
            alert.setId(1L);
            alert.setStatus(AlertStatus.ACKNOWLEDGED);
            when(inventoryAlertRepository.selectById(1L)).thenReturn(alert);

            assertThrows(BizException.class, () -> service.acknowledge(1L, 1L));
        }

        @Test
        @DisplayName("不存在的预警 → 抛异常")
        void acknowledge_notFound_shouldThrow() {
            when(inventoryAlertRepository.selectById(999L)).thenReturn(null);
            assertThrows(BizException.class, () -> service.acknowledge(999L, 1L));
        }
    }

    @Nested
    @DisplayName("无规则场景")
    class NoRuleTests {

        @Test
        @DisplayName("无启用规则 → 不生成预警")
        void noEnabledRules_shouldReturnZero() {
            when(alertRuleRepository.selectAllEnabled()).thenReturn(Collections.emptyList());

            int count = service.scanAndAlert();
            assertEquals(0, count);
        }
    }
}
