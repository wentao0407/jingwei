package com.jingwei.order.domain.service;

import com.jingwei.common.domain.model.DomainEvent;
import com.jingwei.common.domain.service.DomainEventPublisher;
import com.jingwei.common.statemachine.TransitionContext;
import com.jingwei.inventory.domain.model.InventoryAllocation;
import com.jingwei.inventory.domain.model.InventorySku;
import com.jingwei.inventory.domain.repository.InventoryAllocationRepository;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import com.jingwei.inventory.domain.service.AllocationDomainService;
import com.jingwei.master.domain.model.Sku;
import com.jingwei.master.domain.repository.SkuRepository;
import com.jingwei.order.domain.model.SalesOrder;
import com.jingwei.order.domain.model.SalesOrderEvent;
import com.jingwei.order.domain.model.SalesOrderLine;
import com.jingwei.order.domain.model.SalesOrderStatus;
import com.jingwei.order.domain.model.SizeMatrix;
import com.jingwei.order.domain.repository.SalesOrderLineRepository;
import com.jingwei.order.domain.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 销售订单状态机动作执行器
 * <p>
 * 将转移动作逻辑与状态机配置分离，动作中可注入任意 Service/Repository，
 * 不会让状态机配置类膨胀。每个动作方法对应一条转移成功后需要执行的副作用，
 * 如发布领域事件、触发库存预留等。
 * </p>
 * <p>
 * 跨模块通信用领域事件（Outbox），通过 {@link DomainEventPublisher} 写入 Outbox 表，
 * 由 OutboxEventRelay 投递到 Spring Event Bus。
 * </p>
 * <p>
 * 重要：动作在状态机 fireEvent 内执行，调用方通常在 @Transactional 事务内调用 fireEvent，
 * 动作异常会导致事务回滚，保证状态变更与副作用的一致性。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesOrderActionExecutor {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final AllocationDomainService allocationDomainService;
    private final InventoryAllocationRepository inventoryAllocationRepository;
    private final InventorySkuRepository inventorySkuRepository;
    private final SkuRepository skuRepository;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * 订单审批通过后的动作
     * <p>
     * 触发库存预留：通知库存模块，将可用库存锁定给本订单。
     * 领域事件：SalesOrderConfirmedEvent
     * </p>
     * <p>
     * TODO: T-20/T-30 实现后，通过 Outbox 发布 SalesOrderConfirmedEvent，
     *       库存模块订阅该事件触发库存预留
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     */
    public void onOrderConfirmed(TransitionContext<SalesOrderStatus, SalesOrderEvent> context) {
        Long salesOrderId = context.getBusinessId();
        Long operatorId = context.getOperatorId();

        SalesOrder order = salesOrderRepository.selectById(salesOrderId);
        if (order == null) {
            log.warn("销售订单不存在, 跳过库存预留: salesOrderId={}", salesOrderId);
            return;
        }

        List<SalesOrderLine> lines = salesOrderLineRepository.selectByOrderId(salesOrderId);
        for (SalesOrderLine line : lines) {
            if (line.getSizeMatrix() != null && line.getSizeMatrix().getSizes() != null) {
                // 按尺码矩阵精确定位 SKU 并预留，每个尺码独立处理
                List<Sku> spuSkus = skuRepository.selectBySpuId(line.getSpuId());
                for (SizeMatrix.SizeEntry sizeEntry : line.getSizeMatrix().getSizes()) {
                    int qty = sizeEntry.getQuantity();
                    if (qty <= 0) continue;

                    // 通过 (spuId, colorWayId, sizeId) 精确匹配 SKU
                    Long skuId = spuSkus.stream()
                            .filter(s -> line.getColorWayId().equals(s.getColorWayId())
                                    && sizeEntry.getSizeId().equals(s.getSizeId()))
                            .map(Sku::getId)
                            .findFirst().orElse(null);
                    if (skuId == null) {
                        log.warn("SKU不存在, 跳过预留: spuId={}, colorWayId={}, sizeId={}",
                                line.getSpuId(), line.getColorWayId(), sizeEntry.getSizeId());
                        continue;
                    }

                    reserveForSku(skuId, qty, salesOrderId, line.getId(), operatorId);
                }
            } else {
                // 尺码矩阵为空时的兜底：按总数量从该 SPU 下所有 SKU 中预留
                log.warn("销售行尺码矩阵为空, 按SPU兜底预留: lineId={}, spuId={}", line.getId(), line.getSpuId());
                int demandQty = line.getTotalQuantity();
                if (demandQty <= 0) continue;
                List<Sku> skus = skuRepository.selectBySpuId(line.getSpuId());
                int remaining = demandQty;
                for (Sku sku : skus) {
                    if (remaining <= 0) break;
                    remaining -= reserveForSku(sku.getId(), remaining, salesOrderId, line.getId(), operatorId);
                }
                if (remaining > 0) {
                    log.warn("库存部分预留: salesOrderId={}, lineId={}, 需求={}, 缺口={}",
                            salesOrderId, line.getId(), demandQty, remaining);
                }
            }
        }

        log.info("销售订单库存预留完成: salesOrderId={}, 行数={}", salesOrderId, lines.size());
    }

    /**
     * 为指定 SKU 预留库存，按仓库可用量依次锁定
     *
     * @return 实际预留数量
     */
    private int reserveForSku(Long skuId, int demandQty, Long salesOrderId, Long lineId, Long operatorId) {
        List<InventorySku> allInventory = inventorySkuRepository.selectBySkuId(skuId);
        Map<Long, List<InventorySku>> byWarehouse = allInventory.stream()
                .filter(inv -> inv.getAvailableQty() > 0)
                .collect(Collectors.groupingBy(InventorySku::getWarehouseId));

        int remaining = demandQty;
        for (Map.Entry<Long, List<InventorySku>> entry : byWarehouse.entrySet()) {
            if (remaining <= 0) break;
            Long warehouseId = entry.getKey();
            int warehouseAvailable = entry.getValue().stream().mapToInt(InventorySku::getAvailableQty).sum();
            int toReserve = Math.min(remaining, warehouseAvailable);
            if (toReserve <= 0) continue;

            AllocationDomainService.AllocationResult result = allocationDomainService.allocate(
                    skuId, warehouseId, toReserve,
                    "SALES", salesOrderId, lineId, operatorId);
            remaining -= result.getAllocatedQty();
        }
        return demandQty - remaining;
    }

    /**
     * 订单取消后的动作
     * <p>
     * 释放库存预留：通知库存模块，将锁定的库存释放回可用库存。
     * 领域事件：SalesOrderCancelledEvent
     * </p>
     * <p>
     * TODO: T-20/T-30 实现后，通过 Outbox 发布 SalesOrderCancelledEvent，
     *       库存模块订阅该事件释放库存预留
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     */
    public void onOrderCancelled(TransitionContext<SalesOrderStatus, SalesOrderEvent> context) {
        Long salesOrderId = context.getBusinessId();
        Long operatorId = context.getOperatorId();

        // 查询该订单的所有预留记录并释放
        List<InventoryAllocation> allocations = inventoryAllocationRepository.selectByOrder("SALES", salesOrderId);
        for (InventoryAllocation allocation : allocations) {
            try {
                allocationDomainService.release(allocation.getId(), operatorId);
            } catch (Exception e) {
                log.warn("释放库存预留失败: allocationId={}, error={}", allocation.getId(), e.getMessage());
            }
        }

        log.info("销售订单库存预留已释放: salesOrderId={}, 释放记录数={}", salesOrderId, allocations.size());
    }

    /**
     * 订单开始排产后的动作
     * <p>
     * 通知采购模块：销售订单已进入排产，采购模块可据此准备物料采购。
     * 领域事件：SalesOrderProducingEvent
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     */
    public void onOrderProducing(TransitionContext<SalesOrderStatus, SalesOrderEvent> context) {
        domainEventPublisher.publish(DomainEvent.of("SalesOrderProducing", "SALES_ORDER",
                context.getBusinessId(), Map.of(
                        "salesOrderId", context.getBusinessId(),
                        "operatorId", context.getOperatorId()
                )));
        log.info("销售订单排产事件已发布, orderId={}, operatorId={}",
                context.getBusinessId(), context.getOperatorId());
    }

    /**
     * 生产完工入库后的动作
     * <p>
     * 通知销售模块：关联的生产订单已完工入库，销售订单可以准备发货。
     * 领域事件：ProductionStockedEvent（由生产订单侧发出，此处预留处理逻辑）
     * </p>
     * <p>
     * TODO: T-23/T-24 实现后，接收生产订单入库事件，更新销售订单状态
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     */
    public void onProductionStocked(TransitionContext<SalesOrderStatus, SalesOrderEvent> context) {
        // TODO: 由生产订单入库事件触发，更新销售订单状态为 READY
        log.info("[预留] 生产完工入库, 销售订单可发货, orderId={}, operatorId={}",
                context.getBusinessId(), context.getOperatorId());
    }
}
