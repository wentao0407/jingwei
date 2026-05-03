package com.jingwei.order.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.statemachine.TransitionContext;
import com.jingwei.order.domain.model.ProductionOrderEvent;
import com.jingwei.order.domain.model.ProductionOrderLine;
import com.jingwei.order.domain.model.ProductionOrderStatus;
import com.jingwei.order.domain.repository.ProductionOrderLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 生产订单状态机前置条件评估器
 * <p>
 * 将条件逻辑与状态机配置分离，条件中可注入任意 Service/Repository，
 * 不会让状态机配置类膨胀。每个条件方法对应一条转移的前置校验，
 * 不满足时直接抛出 {@link BizException}，携带具体业务原因。
 * </p>
 * <p>
 * 条件实现状态：
 * <ul>
 *   <li>hasBomAndQuantity — 已实现，检查每行是否有BOM和数量</li>
 *   <li>skipCutting — 已实现，检查行是否配置跳过裁剪</li>
 *   <li>allStockedIn — 预留，待 T-29/T-30 库存管理实现</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductionOrderConditionEvaluator {

    private final ProductionOrderLineRepository productionOrderLineRepository;

    /**
     * 检查订单行是否有BOM和数量
     * <p>
     * 用于：DRAFT → RELEASED（下达生产订单）的前置条件。
     * 每行必须关联BOM（bom_id不为空）且有数量（totalQuantity > 0），
     * 否则不允许下达。
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     * @return true 表示所有行都有BOM和数量
     * @throws BizException 有行缺少BOM或数量时抛出
     */
    public boolean hasBomAndQuantity(TransitionContext<ProductionOrderStatus, ProductionOrderEvent> context) {
        List<ProductionOrderLine> lines = productionOrderLineRepository.selectByOrderId(context.getBusinessId());

        if (lines.isEmpty()) {
            throw new BizException(ErrorCode.ORDER_LINE_EMPTY);
        }

        for (ProductionOrderLine line : lines) {
            if (line.getBomId() == null) {
                throw new BizException(ErrorCode.ORDER_LINE_INCOMPLETE,
                        "第" + line.getLineNo() + "行未关联BOM，无法下达");
            }
            if (line.getTotalQuantity() == null || line.getTotalQuantity() <= 0) {
                throw new BizException(ErrorCode.ORDER_LINE_INCOMPLETE,
                        "第" + line.getLineNo() + "行数量为空，无法下达");
            }
        }

        return true;
    }

    /**
     * 检查行是否跳过裁剪环节
     * <p>
     * 用于：
     * <ul>
     *   <li>PLANNED → CUTTING（开始裁剪）的前置条件 — 返回 false 时允许</li>
     *   <li>PLANNED → SEWING（跳过裁剪直接缝制）的前置条件 — 返回 true 时允许</li>
     * </ul>
     * 针织类款式通常跳过裁剪，直接进入缝制。
     * </p>
     * <p>
     * 注意：此方法需要从上下文中获取行ID，因为同一订单不同行可能有不同的 skipCutting 配置。
     * 调用方需在 context 中设置 lineId。
     * </p>
     *
     * @param context 转移上下文，需额外设置 lineId
     * @return true 表示跳过裁剪
     */
    public boolean skipCutting(TransitionContext<ProductionOrderStatus, ProductionOrderEvent> context) {
        // 从上下文获取行ID（需要调用方设置）
        Long lineId = context.getParam("lineId");
        if (lineId == null) {
            log.warn("skipCutting 检查缺少 lineId, orderId={}", context.getBusinessId());
            return false;
        }

        ProductionOrderLine line = productionOrderLineRepository.selectById(lineId);
        if (line == null) {
            log.warn("skipCutting 检查找不到行, lineId={}", lineId);
            return false;
        }

        return Boolean.TRUE.equals(line.getSkipCutting());
    }

    /**
     * 检查是否全部入库
     * <p>
     * 用于：COMPLETED → STOCKED（入库完成）的前置条件。
     * 所有行的已入库数量必须等于总数量。
     * </p>
     * <p>
     * TODO: T-29/T-30 库存管理实现后替换为真实逻辑
     * </p>
     *
     * @param context 转移上下文，businessId 为订单ID
     * @return true 表示全部入库
     */
    public boolean allStockedIn(TransitionContext<ProductionOrderStatus, ProductionOrderEvent> context) {
        // 预留钩子：T-29/T-30 实现后，检查入库单是否全部完成
        log.debug("[预留] allStockedIn 检查, orderId={}", context.getBusinessId());
        return true;
    }
}
