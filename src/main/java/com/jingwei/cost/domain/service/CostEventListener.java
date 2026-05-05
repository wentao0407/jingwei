package com.jingwei.cost.domain.service;

import com.jingwei.common.domain.model.DomainEvent;
import com.jingwei.cost.domain.model.MaterialType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 成本事件监听器
 * <p>
 * 监听库存模块发布的领料出库和生产入库事件，
 * 自动调用 {@link CostDomainService} 记录成本数据。
 * </p>
 * <p>
 * 事件来源：{@link com.jingwei.inventory.domain.service.InventoryDomainService}
 * 当 sourceType=PRODUCTION_ORDER 时，发布 MaterialIssued / ProductionInbound 事件。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CostEventListener {

    private final CostDomainService costDomainService;

    /**
     * 领料出库事件 → 记录领料成本
     */
    @EventListener(condition = "#event.eventType == 'MaterialIssued'")
    public void onMaterialIssued(DomainEvent event) {
        Map<String, Object> payload = event.getPayload();
        Long productionOrderId = event.getAggregateId();
        Long materialId = getLong(payload, "materialId");
        BigDecimal issueQty = getBigDecimal(payload, "issueQty");
        BigDecimal unitCost = getBigDecimal(payload, "unitCost");

        if (productionOrderId == null || materialId == null) {
            log.warn("领料成本事件缺少必要参数，跳过: eventId={}", event.getEventId());
            return;
        }

        try {
            costDomainService.recordMaterialIssue(
                    productionOrderId, null, materialId, MaterialType.MATERIAL,
                    issueQty, unitCost, null);
            log.debug("领料成本已记录: productionOrderId={}, materialId={}, qty={}",
                    productionOrderId, materialId, issueQty);
        } catch (Exception e) {
            log.error("领料成本记录失败（不影响库存事务）: productionOrderId={}, materialId={}, error={}",
                    productionOrderId, materialId, e.getMessage(), e);
        }
    }

    /**
     * 生产入库事件 → 计算成品单位成本
     */
    @EventListener(condition = "#event.eventType == 'ProductionInbound'")
    public void onProductionInbound(DomainEvent event) {
        Map<String, Object> payload = event.getPayload();
        Long productionOrderId = event.getAggregateId();
        BigDecimal inboundQty = getBigDecimal(payload, "inboundQty");

        if (productionOrderId == null || inboundQty == null || inboundQty.intValue() <= 0) {
            log.warn("生产入库成本事件参数无效，跳过: eventId={}", event.getEventId());
            return;
        }

        try {
            costDomainService.calculateUnitCost(productionOrderId, null, inboundQty.intValue());
            log.debug("成品单位成本已计算: productionOrderId={}, qty={}", productionOrderId, inboundQty);
        } catch (Exception e) {
            log.error("成品成本计算失败（不影响库存事务）: productionOrderId={}, error={}",
                    productionOrderId, e.getMessage(), e);
        }
    }

    private Long getLong(Map<String, Object> payload, String key) {
        Object val = payload.get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        return null;
    }

    private BigDecimal getBigDecimal(Map<String, Object> payload, String key) {
        Object val = payload.get(key);
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        return null;
    }
}
