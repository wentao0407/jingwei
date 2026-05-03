package com.jingwei.order.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.order.domain.model.ProductionOrder;
import com.jingwei.order.domain.model.ProductionOrderLine;
import com.jingwei.order.domain.model.ProductionOrderStatus;
import com.jingwei.order.domain.repository.ProductionOrderLineRepository;
import com.jingwei.order.domain.repository.ProductionOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 生产订单领域服务
 * <p>
 * 核心职责：
 * <ul>
 *   <li>生产订单创建、编辑、删除的业务校验</li>
 *   <li>订单行唯一性校验（同订单内款式+颜色不可重复）</li>
 *   <li>主表状态自动计算（取所有行的最滞后状态）</li>
 *   <li>尺码矩阵校验</li>
 * </ul>
 * </p>
 * <p>
 * 生产订单没有金额字段（不涉及定价），但有生产进度跟踪：
 * completed_quantity、stocked_quantity。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionOrderDomainService {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionOrderLineRepository productionOrderLineRepository;

    /**
     * 获取仓库引用（供 ApplicationService 分页查询使用）
     */
    public ProductionOrderRepository getProductionOrderRepository() {
        return productionOrderRepository;
    }

    /**
     * 创建生产订单
     * <p>
     * 校验规则：
     * <ol>
     *   <li>至少包含一行明细</li>
     *   <li>同一订单内不允许重复的款式+颜色组合</li>
     *   <li>每行的尺码矩阵必须有效</li>
     * </ol>
     * </p>
     *
     * @param order 生产订单实体
     * @param lines 订单行列表
     * @return 保存后的生产订单实体
     */
    public ProductionOrder createOrder(ProductionOrder order, List<ProductionOrderLine> lines) {
        // 1. 至少一行明细
        if (lines == null || lines.isEmpty()) {
            throw new BizException(ErrorCode.ORDER_LINE_EMPTY);
        }

        // 2. 行唯一性校验
        validateLineUniqueness(lines);

        // 3. 校验每行尺码矩阵
        for (ProductionOrderLine line : lines) {
            if (line.getSizeMatrix() != null && !line.getSizeMatrix().validate()) {
                throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "订单行尺码矩阵数据不合法");
            }
            // 自动计算行总数量
            if (line.getSizeMatrix() != null) {
                line.setTotalQuantity(line.getSizeMatrix().getTotalQuantity());
            } else {
                line.setTotalQuantity(0);
            }
            // 行状态默认 DRAFT
            if (line.getStatus() == null) {
                line.setStatus(ProductionOrderStatus.DRAFT);
            }
            // skip_cutting 默认 false
            if (line.getSkipCutting() == null) {
                line.setSkipCutting(false);
            }
        }

        // 4. 计算订单汇总
        calculateOrderSummary(order, lines);

        // 5. 主表状态默认 DRAFT
        order.setStatus(ProductionOrderStatus.DRAFT);

        try {
            productionOrderRepository.insert(order);
        } catch (DuplicateKeyException e) {
            log.warn("并发创建生产订单触发唯一约束: orderNo={}", order.getOrderNo());
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "生产订单编号已存在");
        }

        // 6. 保存订单行
        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).setOrderId(order.getId());
            lines.get(i).setLineNo(i + 1);
        }
        productionOrderLineRepository.batchInsert(lines);

        log.info("创建生产订单: orderNo={}, sourceType={}, totalQuantity={}",
                order.getOrderNo(), order.getSourceType(), order.getTotalQuantity());

        order.setLines(lines);
        return order;
    }

    /**
     * 编辑生产订单
     * <p>
     * 仅 DRAFT 状态允许编辑。采用"先删后插"策略。
     * </p>
     *
     * @param orderId    订单ID
     * @param order      包含更新字段的订单实体
     * @param lines      新的订单行列表
     * @param operatorId 操作人ID
     * @return 更新后的订单
     */
    public ProductionOrder updateOrder(Long orderId, ProductionOrder order,
                                        List<ProductionOrderLine> lines, Long operatorId) {
        ProductionOrder existing = productionOrderRepository.selectById(orderId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "生产订单不存在");
        }

        if (existing.getStatus() != ProductionOrderStatus.DRAFT) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "只有草稿状态的订单允许编辑");
        }

        if (lines == null || lines.isEmpty()) {
            throw new BizException(ErrorCode.ORDER_LINE_EMPTY);
        }

        validateLineUniqueness(lines);

        // 校验并计算每行
        for (ProductionOrderLine line : lines) {
            if (line.getSizeMatrix() != null && !line.getSizeMatrix().validate()) {
                throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "订单行尺码矩阵数据不合法");
            }
            if (line.getSizeMatrix() != null) {
                line.setTotalQuantity(line.getSizeMatrix().getTotalQuantity());
            } else {
                line.setTotalQuantity(0);
            }
            if (line.getStatus() == null) {
                line.setStatus(ProductionOrderStatus.DRAFT);
            }
            if (line.getSkipCutting() == null) {
                line.setSkipCutting(false);
            }
        }

        calculateOrderSummary(order, lines);

        // 保留不可修改的字段
        order.setId(orderId);
        order.setOrderNo(existing.getOrderNo());
        order.setStatus(existing.getStatus());

        int rows = productionOrderRepository.updateById(order);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        // 先删后插
        productionOrderLineRepository.deleteByOrderId(orderId);
        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).setOrderId(orderId);
            lines.get(i).setLineNo(i + 1);
        }
        productionOrderLineRepository.batchInsert(lines);

        log.info("编辑生产订单: orderId={}, orderNo={}", orderId, existing.getOrderNo());

        return productionOrderRepository.selectDetailById(orderId);
    }

    /**
     * 删除生产订单
     * <p>
     * 仅 DRAFT 状态允许删除。
     * </p>
     *
     * @param orderId 订单ID
     */
    public void deleteOrder(Long orderId) {
        ProductionOrder existing = productionOrderRepository.selectById(orderId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "生产订单不存在");
        }

        if (existing.getStatus() != ProductionOrderStatus.DRAFT) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "只有草稿状态的订单允许删除");
        }

        productionOrderLineRepository.deleteByOrderId(orderId);
        productionOrderRepository.deleteById(orderId);

        log.info("删除生产订单: orderId={}, orderNo={}", orderId, existing.getOrderNo());
    }

    /**
     * 根据ID查询订单详情（含行）
     *
     * @param orderId 订单ID
     * @return 订单实体（含行）
     */
    public ProductionOrder getOrderDetail(Long orderId) {
        ProductionOrder order = productionOrderRepository.selectDetailById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "生产订单不存在");
        }
        return order;
    }

    /**
     * 重新计算主表状态
     * <p>
     * 遍历所有行，取最滞后状态（ordinal 最小）作为主表状态。
     * 每次行状态变更后调用。
     * </p>
     *
     * @param orderId 订单ID
     */
    public void recalculateMainStatus(Long orderId) {
        ProductionOrder order = productionOrderRepository.selectById(orderId);
        if (order == null) {
            return;
        }

        List<ProductionOrderLine> lines = productionOrderLineRepository.selectByOrderId(orderId);
        if (lines.isEmpty()) {
            return;
        }

        // 取最滞后状态（order 值最小）
        ProductionOrderStatus mostBehind = lines.stream()
                .map(ProductionOrderLine::getStatus)
                .min(Comparator.comparingInt(ProductionOrderStatus::getOrder))
                .orElse(ProductionOrderStatus.DRAFT);

        if (order.getStatus() != mostBehind) {
            order.setStatus(mostBehind);
            productionOrderRepository.updateById(order);
            log.info("生产订单主表状态更新: orderId={}, newStatus={}", orderId, mostBehind.getLabel());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 校验订单行唯一性
     */
    private void validateLineUniqueness(List<ProductionOrderLine> lines) {
        Set<String> seen = new HashSet<>();
        for (ProductionOrderLine line : lines) {
            String key = line.getSpuId() + "_" + line.getColorWayId();
            if (!seen.add(key)) {
                throw new BizException(ErrorCode.ORDER_LINE_DUPLICATE);
            }
        }
    }

    /**
     * 计算订单汇总字段
     */
    private void calculateOrderSummary(ProductionOrder order, List<ProductionOrderLine> lines) {
        int totalQuantity = lines.stream()
                .mapToInt(ProductionOrderLine::getTotalQuantity)
                .sum();
        order.setTotalQuantity(totalQuantity);
        order.setCompletedQuantity(0);
        order.setStockedQuantity(0);
    }
}
