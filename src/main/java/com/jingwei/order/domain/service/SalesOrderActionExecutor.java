package com.jingwei.order.domain.service;

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
 * 当前阶段（T-17）跨模块通信用 Outbox + Spring Event，但 Outbox 模块尚未实现（T-40），
 * 因此动作方法暂时只打日志。待 T-40 实现后替换为 Outbox 写入。
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
            int demandQty = line.getTotalQuantity();
            if (demandQty <= 0) continue;

            // 通过 SPU 查找关联的 SKU 列表，逐 SKU 预留
            List<Sku> skus = skuRepository.selectBySpuId(line.getSpuId());
            if (skus.isEmpty()) {
                log.warn("SPU 下无 SKU, 跳过预留: salesOrderId={}, spuId={}", salesOrderId, line.getSpuId());
                continue;
            }

            int remaining = demandQty;
            for (Sku sku : skus) {
                if (remaining <= 0) break;

                List<InventorySku> allInventory = inventorySkuRepository.selectBySkuId(sku.getId());
                Map<Long, List<InventorySku>> byWarehouse = allInventory.stream()
                        .filter(inv -> inv.getAvailableQty() > 0)
                        .collect(Collectors.groupingBy(InventorySku::getWarehouseId));

                for (Map.Entry<Long, List<InventorySku>> entry : byWarehouse.entrySet()) {
                    if (remaining <= 0) break;
                    Long warehouseId = entry.getKey();
                    int warehouseAvailable = entry.getValue().stream().mapToInt(InventorySku::getAvailableQty).sum();
                    int toReserve = Math.min(remaining, warehouseAvailable);
                    if (toReserve <= 0) continue;

                    AllocationDomainService.AllocationResult result = allocationDomainService.allocate(
                            sku.getId(), warehouseId, toReserve,
                            "SALES", salesOrderId, line.getId(), operatorId);
                    remaining -= result.getAllocatedQty();
                }
            }

            if (remaining > 0) {
                log.warn("库存部分预留: salesOrderId={}, spuId={}, 需求={}, 缺口={}",
                        salesOrderId, line.getSpuId(), demandQty, remaining);
            }
        }

        log.info("销售订单库存预留完成: salesOrderId={}, 行数={}", salesOrderId, lines.size());
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
     * <p>
     * TODO: T-20/T-25 实现后，通过 Outbox 发布 SalesOrderProducingEvent，
     *       采购模块订阅该事件触发 MRP 计算
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     */
    public void onOrderProducing(TransitionContext<SalesOrderStatus, SalesOrderEvent> context) {
        // TODO: T-40 Outbox 实现后替换
        // outboxRepository.save(DomainEvent.of(
        //     "SalesOrderProducing",
        //     context.getBusinessId(),
        //     Map.of("salesOrderId", context.getBusinessId(),
        //            "operatorId", context.getOperatorId())
        // ));
        log.info("[预留] 销售订单开始排产, 通知采购模块, orderId={}, operatorId={}",
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
