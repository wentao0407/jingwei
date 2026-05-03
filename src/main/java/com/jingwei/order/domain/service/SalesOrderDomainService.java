package com.jingwei.order.domain.service;

import com.jingwei.approval.domain.service.ApprovalDomainService;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.statemachine.StateMachine;
import com.jingwei.common.statemachine.TransitionContext;
import com.jingwei.order.domain.model.SalesOrder;
import com.jingwei.order.domain.model.SalesOrderEvent;
import com.jingwei.order.domain.model.SalesOrderLine;
import com.jingwei.order.domain.model.SalesOrderStatus;
import com.jingwei.order.domain.model.SizeMatrix;
import com.jingwei.order.domain.repository.SalesOrderLineRepository;
import com.jingwei.order.domain.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 销售订单领域服务
 * <p>
 * 核心职责：
 * <ul>
 *   <li>销售订单创建、编辑、删除的业务校验</li>
 *   <li>订单行唯一性校验（同订单内款式+颜色不可重复）</li>
 *   <li>尺码矩阵校验（矩阵数据完整性）</li>
 *   <li>金额自动计算（行金额、行实际金额、订单总金额）</li>
 *   <li>订单状态校验（只有 DRAFT 状态可编辑）</li>
 *   <li>状态流转（通过状态机引擎驱动）</li>
 * </ul>
 * </p>
 * <p>
 * 关键业务规则：
 * <ul>
 *   <li>订单编号由编码规则引擎自动生成，不可手动指定</li>
 *   <li>同一订单内不允许重复的款式+颜色组合</li>
 *   <li>行金额 = total_quantity × unit_price</li>
 *   <li>行实际金额 = 行金额 × discount_rate</li>
 *   <li>订单总金额 = 所有行实际金额之和</li>
 *   <li>只有 DRAFT 状态允许编辑</li>
 *   <li>创建时必须有至少一行明细</li>
 *   <li>状态流转通过状态机引擎驱动，保证转移合法性</li>
 *   <li>提交和重新提交时调用审批引擎，自动通过则直接推进状态</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderDomainService {

    private static final String SALES_ORDER_APPROVAL_TYPE = "SALES_ORDER";

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final StateMachine<SalesOrderStatus, SalesOrderEvent> salesOrderStateMachine;
    private final ApprovalDomainService approvalDomainService;

    /**
     * 获取仓库引用（供 ApplicationService 分页查询使用）
     *
     * @return 销售订单仓库
     */
    public SalesOrderRepository getSalesOrderRepository() {
        return salesOrderRepository;
    }

    /**
     * 创建销售订单
     * <p>
     * 校验规则：
     * <ol>
     *   <li>订单编号由编码规则引擎生成，不可为空</li>
     *   <li>订单编号不可重复</li>
     *   <li>至少包含一行明细</li>
     *   <li>同一订单内不允许重复的款式+颜色组合</li>
     *   <li>每行的尺码矩阵必须有效</li>
     * </ol>
     * </p>
     *
     * @param order 销售订单实体（orderNo 应由调用方从编码规则引擎获取后设置）
     * @param lines 订单行列表
     * @return 保存后的销售订单实体
     */
    public SalesOrder createOrder(SalesOrder order, List<SalesOrderLine> lines) {
        // 1. 订单编号校验
        if (order.getOrderNo() == null || order.getOrderNo().isBlank()) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "订单编号不能为空");
        }

        // 2. 至少一行明细
        if (lines == null || lines.isEmpty()) {
            throw new BizException(ErrorCode.ORDER_LINE_EMPTY);
        }

        // 3. 行唯一性校验
        validateLineUniqueness(lines);

        // 4. 校验并计算每行金额
        for (SalesOrderLine line : lines) {
            validateAndCalculateLine(line);
        }

        // 5. 计算订单汇总字段
        calculateOrderSummary(order, lines);

        // 6. 新建订单默认 DRAFT 状态
        order.setStatus(SalesOrderStatus.DRAFT);

        try {
            salesOrderRepository.insert(order);
        } catch (DuplicateKeyException e) {
            log.warn("并发创建订单触发唯一约束: orderNo={}", order.getOrderNo());
            throw new BizException(ErrorCode.DATA_ALREADY_EXISTS, "订单编号已存在");
        }

        // 7. 保存订单行
        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).setOrderId(order.getId());
            lines.get(i).setLineNo(i + 1);
        }
        salesOrderLineRepository.batchInsert(lines);

        log.info("创建销售订单: orderNo={}, customerId={}, totalQuantity={}, totalAmount={}",
                order.getOrderNo(), order.getCustomerId(),
                order.getTotalQuantity(), order.getTotalAmount());

        order.setLines(lines);
        return order;
    }

    /**
     * 编辑草稿订单
     * <p>
     * 仅 DRAFT 状态的订单允许编辑。
     * 编辑采用"先删后插"策略：先删除所有旧行，再插入新行。
     * </p>
     *
     * @param orderId 订单ID
     * @param order   包含更新字段的订单实体
     * @param lines   新的订单行列表
     * @return 更新后的订单
     */
    public SalesOrder updateOrder(Long orderId, SalesOrder order, List<SalesOrderLine> lines) {
        SalesOrder existing = salesOrderRepository.selectById(orderId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "销售订单不存在");
        }

        // 只有 DRAFT 和 REJECTED 状态可编辑（驳回后需修改再重新提交）
        if (existing.getStatus() != SalesOrderStatus.DRAFT
                && existing.getStatus() != SalesOrderStatus.REJECTED) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "只有草稿或驳回状态的订单允许编辑");
        }

        // 至少一行明细
        if (lines == null || lines.isEmpty()) {
            throw new BizException(ErrorCode.ORDER_LINE_EMPTY);
        }

        // 行唯一性校验
        validateLineUniqueness(lines);

        // 校验并计算每行金额
        for (SalesOrderLine line : lines) {
            validateAndCalculateLine(line);
        }

        // 计算订单汇总
        calculateOrderSummary(order, lines);

        // 保留不可修改的字段
        order.setId(orderId);
        order.setOrderNo(existing.getOrderNo());
        order.setStatus(existing.getStatus());

        int rows = salesOrderRepository.updateById(order);
        if (rows == 0) {
            throw new BizException(ErrorCode.CONCURRENT_CONFLICT);
        }

        // 先删后插：删除旧行，插入新行
        salesOrderLineRepository.deleteByOrderId(orderId);
        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).setOrderId(orderId);
            lines.get(i).setLineNo(i + 1);
        }
        salesOrderLineRepository.batchInsert(lines);

        log.info("编辑销售订单: orderId={}, orderNo={}", orderId, existing.getOrderNo());

        SalesOrder updated = salesOrderRepository.selectDetailById(orderId);
        return updated;
    }

    /**
     * 删除草稿订单
     * <p>
     * 仅 DRAFT 状态的订单允许删除。
     * </p>
     *
     * @param orderId 订单ID
     */
    public void deleteOrder(Long orderId) {
        SalesOrder existing = salesOrderRepository.selectById(orderId);
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "销售订单不存在");
        }

        if (existing.getStatus() != SalesOrderStatus.DRAFT) {
            throw new BizException(ErrorCode.OPERATION_NOT_ALLOWED, "只有草稿状态的订单允许删除");
        }

        salesOrderLineRepository.deleteByOrderId(orderId);
        salesOrderRepository.deleteById(orderId);

        log.info("删除销售订单: orderId={}, orderNo={}", orderId, existing.getOrderNo());
    }

    /**
     * 根据ID查询订单详情（含行）
     *
     * @param orderId 订单ID
     * @return 订单实体（含行）
     */
    public SalesOrder getOrderDetail(Long orderId) {
        SalesOrder order = salesOrderRepository.selectDetailById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "销售订单不存在");
        }
        return order;
    }

    // ==================== 状态流转方法 ====================

    /**
     * 提交订单审批
     * <p>
     * 流程：DRAFT → PENDING_APPROVAL，触发审批引擎。
     * 若审批配置不存在或未启用，自动通过，状态直接推进到 CONFIRMED。
     * </p>
     *
     * @param orderId    订单ID
     * @param operatorId 操作人ID
     */
    public void submitOrder(Long orderId, Long operatorId) {
        SalesOrder order = getExistingOrder(orderId);
        TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx = buildContext(orderId, operatorId);

        // 状态机校验 + 转移：DRAFT → PENDING_APPROVAL
        SalesOrderStatus newStatus = salesOrderStateMachine.fireEvent(
                order.getStatus(), SalesOrderEvent.SUBMIT, ctx);
        order.setStatus(newStatus);
        salesOrderRepository.updateById(order);

        // 调用审批引擎
        boolean needApproval = approvalDomainService.submitForApproval(
                SALES_ORDER_APPROVAL_TYPE, orderId, order.getOrderNo(), operatorId);

        if (!needApproval) {
            // 自动通过：PENDING_APPROVAL → CONFIRMED
            approveOrder(orderId, operatorId);
        }

        log.info("提交销售订单审批: orderId={}, orderNo={}, needApproval={}",
                orderId, order.getOrderNo(), needApproval);
    }

    /**
     * 审批通过
     * <p>
     * 流程：PENDING_APPROVAL → CONFIRMED，触发库存预留（Outbox 预留）。
     * 由审批引擎回调调用。
     * </p>
     *
     * @param orderId    订单ID
     * @param operatorId 操作人ID
     */
    public void approveOrder(Long orderId, Long operatorId) {
        SalesOrder order = getExistingOrder(orderId);
        TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx = buildContext(orderId, operatorId);

        SalesOrderStatus newStatus = salesOrderStateMachine.fireEvent(
                order.getStatus(), SalesOrderEvent.APPROVE, ctx);
        order.setStatus(newStatus);
        salesOrderRepository.updateById(order);

        log.info("销售订单审批通过: orderId={}, orderNo={}", orderId, order.getOrderNo());
    }

    /**
     * 审批驳回
     * <p>
     * 流程：PENDING_APPROVAL → REJECTED。
     * 由审批引擎回调调用。
     * </p>
     *
     * @param orderId    订单ID
     * @param operatorId 操作人ID
     */
    public void rejectOrder(Long orderId, Long operatorId) {
        SalesOrder order = getExistingOrder(orderId);
        TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx = buildContext(orderId, operatorId);

        SalesOrderStatus newStatus = salesOrderStateMachine.fireEvent(
                order.getStatus(), SalesOrderEvent.REJECT, ctx);
        order.setStatus(newStatus);
        salesOrderRepository.updateById(order);

        log.info("销售订单审批驳回: orderId={}, orderNo={}", orderId, order.getOrderNo());
    }

    /**
     * 修改后重新提交
     * <p>
     * 流程：REJECTED → PENDING_APPROVAL，触发审批引擎。
     * 若自动通过则直接推进到 CONFIRMED。
     * </p>
     *
     * @param orderId    订单ID
     * @param operatorId 操作人ID
     */
    public void resubmitOrder(Long orderId, Long operatorId) {
        SalesOrder order = getExistingOrder(orderId);
        TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx = buildContext(orderId, operatorId);

        SalesOrderStatus newStatus = salesOrderStateMachine.fireEvent(
                order.getStatus(), SalesOrderEvent.RESUBMIT, ctx);
        order.setStatus(newStatus);
        salesOrderRepository.updateById(order);

        // 调用审批引擎
        boolean needApproval = approvalDomainService.submitForApproval(
                SALES_ORDER_APPROVAL_TYPE, orderId, order.getOrderNo(), operatorId);

        if (!needApproval) {
            approveOrder(orderId, operatorId);
        }

        log.info("销售订单重新提交: orderId={}, orderNo={}, needApproval={}",
                orderId, order.getOrderNo(), needApproval);
    }

    /**
     * 取消订单
     * <p>
     * 流程：
     * <ul>
     *   <li>DRAFT → CANCELLED（直接取消）</li>
     *   <li>CONFIRMED → CANCELLED（释放库存预留，前提：未关联生产订单）</li>
     * </ul>
     * </p>
     *
     * @param orderId    订单ID
     * @param operatorId 操作人ID
     */
    public void cancelOrder(Long orderId, Long operatorId) {
        SalesOrder order = getExistingOrder(orderId);
        TransitionContext<SalesOrderStatus, SalesOrderEvent> ctx = buildContext(orderId, operatorId);

        SalesOrderStatus newStatus = salesOrderStateMachine.fireEvent(
                order.getStatus(), SalesOrderEvent.CANCEL, ctx);
        order.setStatus(newStatus);
        salesOrderRepository.updateById(order);

        log.info("销售订单已取消: orderId={}, orderNo={}", orderId, order.getOrderNo());
    }

    // ==================== 私有方法 ====================

    /**
     * 校验订单行唯一性：同一订单内不允许重复的款式+颜色组合
     *
     * @param lines 订单行列表
     */
    private void validateLineUniqueness(List<SalesOrderLine> lines) {
        Set<String> seen = new HashSet<>();
        for (SalesOrderLine line : lines) {
            String key = line.getSpuId() + "_" + line.getColorWayId();
            if (!seen.add(key)) {
                throw new BizException(ErrorCode.ORDER_LINE_DUPLICATE);
            }
        }
    }

    /**
     * 校验并计算订单行金额
     * <p>
     * 计算规则：
     * <ul>
     *   <li>totalQuantity = sizeMatrix.totalQuantity</li>
     *   <li>lineAmount = totalQuantity × unitPrice</li>
     *   <li>discountAmount = lineAmount × (1 - discountRate)</li>
     *   <li>actualAmount = lineAmount - discountAmount = lineAmount × discountRate</li>
     * </ul>
     * </p>
     *
     * @param line 订单行
     */
    private void validateAndCalculateLine(SalesOrderLine line) {
        // 校验尺码矩阵
        if (line.getSizeMatrix() != null && !line.getSizeMatrix().validate()) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "订单行尺码矩阵数据不合法");
        }

        // 自动计算行总数量
        if (line.getSizeMatrix() != null) {
            line.setTotalQuantity(line.getSizeMatrix().getTotalQuantity());
        } else {
            line.setTotalQuantity(0);
        }

        // 校验必填字段
        if (line.getUnitPrice() == null) {
            line.setUnitPrice(BigDecimal.ZERO);
        }

        // 折扣率默认 1.0（不打折）
        if (line.getDiscountRate() == null) {
            line.setDiscountRate(BigDecimal.ONE);
        }

        // 计算行金额
        BigDecimal lineAmount = line.getUnitPrice()
                .multiply(BigDecimal.valueOf(line.getTotalQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        line.setLineAmount(lineAmount);

        // 计算折扣金额
        BigDecimal discountAmount = lineAmount
                .multiply(BigDecimal.ONE.subtract(line.getDiscountRate()))
                .setScale(2, RoundingMode.HALF_UP);
        line.setDiscountAmount(discountAmount);

        // 计算实际金额
        BigDecimal actualAmount = lineAmount.subtract(discountAmount);
        line.setActualAmount(actualAmount);
    }

    /**
     * 计算订单汇总字段
     * <p>
     * 汇总规则：
     * <ul>
     *   <li>totalQuantity = 所有行 totalQuantity 之和</li>
     *   <li>totalAmount = 所有行 lineAmount 之和</li>
     *   <li>discountAmount = 所有行 discountAmount 之和</li>
     *   <li>actualAmount = 所有行 actualAmount 之和</li>
     * </ul>
     * </p>
     *
     * @param order 订单实体
     * @param lines 订单行列表
     */
    private void calculateOrderSummary(SalesOrder order, List<SalesOrderLine> lines) {
        int totalQuantity = lines.stream()
                .mapToInt(SalesOrderLine::getTotalQuantity)
                .sum();

        BigDecimal totalAmount = lines.stream()
                .map(SalesOrderLine::getLineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = lines.stream()
                .map(SalesOrderLine::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal actualAmount = lines.stream()
                .map(SalesOrderLine::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalQuantity(totalQuantity);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setActualAmount(actualAmount);
    }

    /**
     * 查询已存在的订单（不存在则抛异常）
     *
     * @param orderId 订单ID
     * @return 订单实体
     */
    private SalesOrder getExistingOrder(Long orderId) {
        SalesOrder order = salesOrderRepository.selectById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "销售订单不存在");
        }
        return order;
    }

    /**
     * 构建状态转移上下文
     *
     * @param orderId    订单ID
     * @param operatorId 操作人ID
     * @return 转移上下文
     */
    private TransitionContext<SalesOrderStatus, SalesOrderEvent> buildContext(Long orderId, Long operatorId) {
        return new TransitionContext<>(orderId, operatorId);
    }
}
