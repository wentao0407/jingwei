package com.jingwei.order.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.statemachine.TransitionContext;
import com.jingwei.order.domain.model.SalesOrderEvent;
import com.jingwei.order.domain.model.SalesOrderStatus;
import com.jingwei.order.domain.repository.ProductionOrderSourceRepository;
import com.jingwei.order.domain.repository.SalesOrderLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 销售订单状态机前置条件评估器
 * <p>
 * 将条件逻辑与状态机配置分离，条件中可注入任意 Service/Repository，
 * 不会让状态机配置类膨胀。每个条件方法对应一条转移的前置校验，
 * 不满足时直接抛出 {@link BizException}，携带具体业务原因。
 * </p>
 * <p>
 * 条件实现状态：
 * <ul>
 *   <li>hasOrderLines — 已实现，查询 SalesOrderLineRepository</li>
 *   <li>hasLinkedProductionOrder — 已实现（T-24），查询 ProductionOrderSourceRepository</li>
 *   <li>hasNoLinkedProductionOrder — 已实现（T-24），查询 ProductionOrderSourceRepository</li>
 *   <li>allStockFulfilled — 预留，待 T-29/T-30 库存管理实现</li>
 *   <li>partialStockFulfilled — 预留，待 T-29/T-30 库存管理实现</li>
 *   <li>hasOutboundOrder — 预留，待 T-31 出入库单实现</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesOrderConditionEvaluator {

    private final SalesOrderLineRepository salesOrderLineRepository;
    private final ProductionOrderSourceRepository productionOrderSourceRepository;

    /**
     * 检查订单是否有明细行
     * <p>
     * 用于：DRAFT → PENDING_APPROVAL（提交审批）和 REJECTED → PENDING_APPROVAL（重新提交）的前置条件。
     * 没有明细行的订单没有业务意义，不允许提交。
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     * @return true 表示有明细行
     * @throws BizException 无明细行时抛出
     */
    public boolean hasOrderLines(TransitionContext<SalesOrderStatus, SalesOrderEvent> context) {
        boolean hasLines = salesOrderLineRepository.existsByOrderId(context.getBusinessId());
        if (!hasLines) {
            throw new BizException(ErrorCode.ORDER_LINE_EMPTY);
        }
        return true;
    }

    /**
     * 检查订单是否已关联生产订单
     * <p>
     * 用于：CONFIRMED → PRODUCING（开始排产）的前置条件。
     * 只有已关联生产订单的销售订单才能进入生产中状态。
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     * @return true 表示已关联生产订单
     * @throws BizException 未关联生产订单时抛出
     */
    public boolean hasLinkedProductionOrder(TransitionContext<SalesOrderStatus, SalesOrderEvent> context) {
        boolean linked = productionOrderSourceRepository.existsBySalesOrderId(context.getBusinessId());
        if (!linked) {
            throw new BizException(ErrorCode.ORDER_LINKED_PRODUCTION,
                    "订单未关联生产订单，无法开始排产");
        }
        return true;
    }

    /**
     * 检查订单是否未关联生产订单
     * <p>
     * 用于：CONFIRMED → CANCELLED（取消订单）的前置条件。
     * 已关联生产订单的确认订单不允许直接取消，需先解除关联。
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     * @return true 表示未关联生产订单
     * @throws BizException 已关联生产订单时抛出
     */
    public boolean hasNoLinkedProductionOrder(TransitionContext<SalesOrderStatus, SalesOrderEvent> context) {
        boolean linked = productionOrderSourceRepository.existsBySalesOrderId(context.getBusinessId());
        if (linked) {
            throw new BizException(ErrorCode.ORDER_LINKED_PRODUCTION,
                    "订单已关联生产订单，无法直接取消");
        }
        return true;
    }

    /**
     * 检查所有SKU库存是否满足
     * <p>
     * 用于：PRODUCING → READY（备货完成）的前置条件。
     * 所有订单行SKU的可用库存均满足订单数量时才能进入备货完成状态。
     * </p>
     * <p>
     * TODO: T-29/T-30 库存管理实现后替换为真实逻辑
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     * @return true 表示所有SKU库存满足
     */
    public boolean allStockFulfilled(TransitionContext<SalesOrderStatus, SalesOrderEvent> context) {
        // 预留钩子：T-29/T-30 实现后，查询库存满足率
        log.debug("[预留] allStockFulfilled 检查, orderId={}", context.getBusinessId());
        return true;
    }

    /**
     * 检查是否有部分库存满足（允许部分发货场景）
     * <p>
     * 用于：PRODUCING → SHIPPED（部分发货）的前置条件。
     * 至少部分SKU库存满足时允许部分发货。
     * </p>
     * <p>
     * TODO: T-29/T-30 库存管理实现后替换为真实逻辑
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     * @return true 表示有部分库存满足
     */
    public boolean partialStockFulfilled(TransitionContext<SalesOrderStatus, SalesOrderEvent> context) {
        // 预留钩子：部分发货场景，检查是否有部分SKU库存满足
        log.debug("[预留] partialStockFulfilled 检查, orderId={}", context.getBusinessId());
        return true;
    }

    /**
     * 检查是否已创建出库单
     * <p>
     * 用于：READY → SHIPPED（发货）的前置条件。
     * 发货前必须已创建出库单，确保仓库作业流程完整。
     * </p>
     * <p>
     * TODO: T-31 出入库单实现后替换为真实逻辑
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     * @return true 表示已创建出库单
     */
    public boolean hasOutboundOrder(TransitionContext<SalesOrderStatus, SalesOrderEvent> context) {
        // 预留钩子：T-31 实现后，查询是否有关联的出库单
        log.debug("[预留] hasOutboundOrder 检查, orderId={}", context.getBusinessId());
        return true;
    }
}
