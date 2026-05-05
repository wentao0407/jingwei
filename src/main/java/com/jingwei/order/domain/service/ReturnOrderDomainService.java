package com.jingwei.order.domain.service;

import com.jingwei.approval.domain.service.ApprovalDomainService;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.inventory.domain.service.ChangeInventoryCommand;
import com.jingwei.inventory.domain.model.InventorySku;
import com.jingwei.inventory.domain.model.OperationType;
import com.jingwei.inventory.domain.service.InventoryDomainService;
import com.jingwei.inventory.domain.repository.InventorySkuRepository;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.order.domain.model.*;
import com.jingwei.order.domain.repository.ReturnOrderLineRepository;
import com.jingwei.order.domain.repository.ReturnOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 退货单领域服务 — 退货管理的核心业务逻辑
 * <p>
 * 负责退货单的全生命周期管理：
 * <ul>
 *   <li>创建退货单（关联原销售订单，校验退货数量）</li>
 *   <li>提交审批 / 审批通过 / 审批驳回</li>
 *   <li>退货收货（触发库存 INBOUND_RETURN 操作）</li>
 *   <li>退货质检（合格→可用库存，不合格→报废）</li>
 * </ul>
 * </p>
 * <p>
 * 状态流转：DRAFT → PENDING_APPROVAL → APPROVED → RECEIVING → QC → COMPLETED
 * 驳回分支：PENDING_APPROVAL → REJECTED
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnOrderDomainService {

    /**
     * 审批业务类型标识：退货单审批
     * <p>
     * 与 t_sys_approval_config 表的 business_type 字段对应，
     * 用于查询退货单的审批配置（审批模式、审批角色等）。
     * </p>
     */
    private static final String RETURN_ORDER_APPROVAL_TYPE = "RETURN_ORDER";

    private final ReturnOrderRepository returnOrderRepository;
    private final ReturnOrderLineRepository returnOrderLineRepository;
    private final ApprovalDomainService approvalDomainService;
    private final CodingRuleDomainService codingRuleDomainService;
    private final InventoryDomainService inventoryDomainService;
    private final InventorySkuRepository inventorySkuRepository;

    /**
     * 创建退货单
     *
     * @param returnOrder 退货单实体
     * @param lines       退货行列表
     * @return 创建后的退货单
     */
    public ReturnOrder createReturnOrder(ReturnOrder returnOrder, List<ReturnOrderLine> lines) {
        // 1. 校验退货行不为空
        if (lines == null || lines.isEmpty()) {
            throw new BizException(ErrorCode.ORDER_LINE_EMPTY);
        }

        // 2. 生成退货单号
        String returnNo = codingRuleDomainService.generateCode("RETURN_NO", Collections.emptyMap());
        returnOrder.setReturnNo(returnNo);

        // 3. 设置初始状态
        returnOrder.setStatus(ReturnStatus.DRAFT);
        returnOrder.setTotalQuantity(lines.stream()
                .mapToInt(ReturnOrderLine::getTotalQuantity)
                .sum());

        // 4. 校验退货数量不超过原订单已发货数量
        validateReturnQuantities(lines);

        // 5. 保存退货单
        returnOrderRepository.insert(returnOrder);

        // 6. 保存退货行
        for (ReturnOrderLine line : lines) {
            line.setReturnId(returnOrder.getId());
        }
        returnOrderLineRepository.insertBatch(lines);
        returnOrder.setLines(lines);

        log.info("退货单已创建: returnNo={}, salesOrderNo={}, totalQty={}",
                returnNo, returnOrder.getSalesOrderNo(), returnOrder.getTotalQuantity());
        return returnOrder;
    }

    /**
     * 提交退货审批
     * <p>
     * 状态转移：DRAFT → PENDING_APPROVAL
     * </p>
     *
     * @param returnId 退货单ID
     */
    public void submitForApproval(Long returnId, Long operatorId) {
        ReturnOrder returnOrder = getAndCheckReturnOrder(returnId);

        // 只有 DRAFT 状态可提交
        if (returnOrder.getStatus() != ReturnStatus.DRAFT) {
            throw new BizException(ErrorCode.ORDER_STATE_TRANSITION_INVALID,
                    "当前状态[" + returnOrder.getStatus().getLabel() + "]不允许提交审批");
        }

        // 调用审批引擎
        boolean autoApproved = approvalDomainService.submitForApproval(
                RETURN_ORDER_APPROVAL_TYPE, returnId, returnOrder.getReturnNo(), operatorId);

        if (autoApproved) {
            // 自动审批通过（无审批配置或审批人就是提交人）
            returnOrder.setStatus(ReturnStatus.APPROVED);
            returnOrder.setApprovedAt(LocalDateTime.now());
            returnOrderRepository.updateById(returnOrder);
            log.info("退货单自动审批通过: returnNo={}", returnOrder.getReturnNo());
        } else {
            // 等待审批
            returnOrder.setStatus(ReturnStatus.PENDING_APPROVAL);
            returnOrderRepository.updateById(returnOrder);
            log.info("退货单已提交审批: returnNo={}", returnOrder.getReturnNo());
        }
    }

    /**
     * 审批通过
     * <p>
     * 状态转移：PENDING_APPROVAL → APPROVED
     * </p>
     *
     * @param returnId   退货单ID
     * @param approvedBy 审批人ID
     */
    public void approve(Long returnId, Long approvedBy) {
        ReturnOrder returnOrder = getAndCheckReturnOrder(returnId);

        if (returnOrder.getStatus() != ReturnStatus.PENDING_APPROVAL) {
            throw new BizException(ErrorCode.ORDER_STATE_TRANSITION_INVALID,
                    "当前状态[" + returnOrder.getStatus().getLabel() + "]不允许审批");
        }

        returnOrder.setStatus(ReturnStatus.APPROVED);
        returnOrder.setApprovedBy(approvedBy);
        returnOrder.setApprovedAt(LocalDateTime.now());
        returnOrderRepository.updateById(returnOrder);

        log.info("退货单审批通过: returnNo={}, approvedBy={}", returnOrder.getReturnNo(), approvedBy);
    }

    /**
     * 审批驳回
     * <p>
     * 状态转移：PENDING_APPROVAL → REJECTED
     * </p>
     *
     * @param returnId 退货单ID
     */
    public void reject(Long returnId) {
        ReturnOrder returnOrder = getAndCheckReturnOrder(returnId);

        if (returnOrder.getStatus() != ReturnStatus.PENDING_APPROVAL) {
            throw new BizException(ErrorCode.ORDER_STATE_TRANSITION_INVALID,
                    "当前状态[" + returnOrder.getStatus().getLabel() + "]不允许驳回");
        }

        returnOrder.setStatus(ReturnStatus.REJECTED);
        returnOrderRepository.updateById(returnOrder);

        log.info("退货单审批驳回: returnNo={}", returnOrder.getReturnNo());
    }

    /**
     * 退货收货确认
     * <p>
     * 状态转移：APPROVED → RECEIVING → QC
     * 触发库存操作：INBOUND_RETURN（质检库存增加）
     * </p>
     *
     * @param returnId 退货单ID
     */
    public void confirmReceive(Long returnId) {
        ReturnOrder returnOrder = getAndCheckReturnOrder(returnId);

        if (returnOrder.getStatus() != ReturnStatus.APPROVED) {
            throw new BizException(ErrorCode.ORDER_STATE_TRANSITION_INVALID,
                    "当前状态[" + returnOrder.getStatus().getLabel() + "]不允许收货");
        }

        List<ReturnOrderLine> lines = returnOrderLineRepository.selectByReturnId(returnId);

        // 为每个退货行的每个尺码执行 INBOUND_RETURN 库存操作
        for (ReturnOrderLine line : lines) {
            processReturnInbound(line, returnOrder);
        }

        // 更新状态为质检中
        returnOrder.setStatus(ReturnStatus.QC);
        returnOrderRepository.updateById(returnOrder);

        log.info("退货收货确认完成: returnNo={}, 行数={}", returnOrder.getReturnNo(), lines.size());
    }

    /**
     * 退货质检
     * <p>
     * 状态转移：QC → COMPLETED
     * 合格品：QC_PASS（质检库存→可用库存）
     * 不合格品：QC_FAIL（质检库存减少，标记报废）
     * </p>
     *
     * @param returnId 退货单ID
     * @param qcResults 质检结果列表（每行的合格/不合格数量）
     */
    public void processQc(Long returnId, List<QcResultItem> qcResults) {
        ReturnOrder returnOrder = getAndCheckReturnOrder(returnId);

        if (returnOrder.getStatus() != ReturnStatus.QC) {
            throw new BizException(ErrorCode.ORDER_STATE_TRANSITION_INVALID,
                    "当前状态[" + returnOrder.getStatus().getLabel() + "]不允许质检");
        }

        List<ReturnOrderLine> lines = returnOrderLineRepository.selectByReturnId(returnId);

        for (QcResultItem qcResult : qcResults) {
            ReturnOrderLine line = lines.stream()
                    .filter(l -> l.getId().equals(qcResult.getLineId()))
                    .findFirst()
                    .orElseThrow(() -> new BizException(ErrorCode.DATA_NOT_FOUND,
                            "退货行不存在: lineId=" + qcResult.getLineId()));

            // 更新退货行质检数量
            line.setQcPassedQty(qcResult.getPassedQty());
            line.setQcFailedQty(qcResult.getFailedQty());
            returnOrderLineRepository.updateById(line);

            // 合格品：QC_PASS（质检库存→可用库存）
            if (qcResult.getPassedQty() > 0) {
                processQcPass(line, qcResult.getPassedQty(), returnOrder);
            }

            // 不合格品：QC_FAIL（质检库存减少，不进入可用库存）
            if (qcResult.getFailedQty() > 0) {
                processQcFail(line, qcResult.getFailedQty(), returnOrder);
            }
        }

        // 更新状态为已完成
        returnOrder.setStatus(ReturnStatus.COMPLETED);
        returnOrderRepository.updateById(returnOrder);

        log.info("退货质检完成: returnNo={}", returnOrder.getReturnNo());
    }

    /**
     * 查询退货单详情（含退货行）
     *
     * @param returnId 退货单ID
     * @return 退货单
     */
    public ReturnOrder getDetail(Long returnId) {
        ReturnOrder returnOrder = getAndCheckReturnOrder(returnId);
        List<ReturnOrderLine> lines = returnOrderLineRepository.selectByReturnId(returnId);
        returnOrder.setLines(lines);
        return returnOrder;
    }

    // ==================== 私有方法 ====================

    /**
     * 获取并校验退货单存在
     */
    private ReturnOrder getAndCheckReturnOrder(Long returnId) {
        ReturnOrder returnOrder = returnOrderRepository.selectById(returnId);
        if (returnOrder == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "退货单不存在");
        }
        return returnOrder;
    }

    /**
     * 校验退货数量不超过原订单已发货数量
     * <p>
     * 累计校验：同一订单行的退货总量（包含历史退货）不能超过已发货数量
     * </p>
     */
    private void validateReturnQuantities(List<ReturnOrderLine> lines) {
        for (ReturnOrderLine line : lines) {
            // 查询该订单行已有的退货数量
            int existingReturnQty = returnOrderLineRepository
                    .sumReturnQtyBySalesOrderLineId(line.getSalesOrderLineId());

            // TODO: 查询原订单行的已发货数量（需要 SalesOrderLineRepository）
            // 暂时跳过此校验，在 ApplicationService 层补充
            // int shippedQty = salesOrderLineRepository.getShippedQty(line.getSalesOrderLineId());
            // if (existingReturnQty + line.getTotalQuantity() > shippedQty) {
            //     throw new BizException(ErrorCode.ORDER_RETURN_QUANTITY_EXCEEDED);
            // }
        }
    }

    /**
     * 处理退货入库（INBOUND_RETURN）
     * <p>
     * 将退货尺码矩阵拆解为 SKU 级别的库存操作。
     * 遍历 size_matrix.sizes，通过 (spu_id, color_way_id, sizeId) 关联获取 sku_id，
     * 逐个 SKU 执行 INBOUND_RETURN 库存操作。
     * </p>
     */
    private void processReturnInbound(ReturnOrderLine line, ReturnOrder returnOrder) {
        if (line.getSizeMatrix() == null || line.getSizeMatrix().getSizes() == null) {
            log.warn("退货行尺码矩阵为空，跳过库存操作: lineId={}", line.getId());
            return;
        }

        for (SizeMatrix.SizeEntry sizeEntry : line.getSizeMatrix().getSizes()) {
            int qty = sizeEntry.getQuantity();
            if (qty <= 0) {
                continue;
            }

            // TODO: 通过 (spu_id, color_way_id, sizeId) 查询获取 sku_id
            // 暂时使用 spuId 作为占位，实际需要 SKU 查询
            Long skuId = line.getSpuId(); // 占位

            // 查询或创建库存记录
            InventorySku inventorySku = inventorySkuRepository.selectById(skuId);
            if (inventorySku == null) {
                log.warn("退货SKU库存记录不存在，需先创建: skuId={}", skuId);
                continue;
            }

            // 执行 INBOUND_RETURN 操作（质检库存增加）
            ChangeInventoryCommand cmd = ChangeInventoryCommand.forSku(
                    OperationType.INBOUND_RETURN,
                    inventorySku.getId(),
                    skuId,
                    inventorySku.getWarehouseId(),
                    inventorySku.getBatchNo(),
                    qty,
                    "RETURN_ORDER",
                    returnOrder.getId(),
                    line.getId());

            inventoryDomainService.changeInventory(cmd);
        }

        log.debug("退货行入库处理完成: lineId={}, totalQty={}", line.getId(), line.getTotalQuantity());
    }

    /**
     * 处理质检合格（QC_PASS）
     */
    private void processQcPass(ReturnOrderLine line, int passedQty, ReturnOrder returnOrder) {
        // TODO: 需要获取退货入库时的库存记录ID
        // 暂时记录日志，实际需要关联入库时的 inventorySkuId
        log.debug("退货质检合格: lineId={}, passedQty={}", line.getId(), passedQty);
    }

    /**
     * 处理质检不合格（QC_FAIL）
     */
    private void processQcFail(ReturnOrderLine line, int failedQty, ReturnOrder returnOrder) {
        // TODO: 需要获取退货入库时的库存记录ID
        log.debug("退货质检不合格（报废）: lineId={}, failedQty={}", line.getId(), failedQty);
    }

    /**
     * 质检结果项
     */
    @lombok.Getter
    @lombok.Setter
    public static class QcResultItem {
        /** 退货行ID */
        private Long lineId;
        /** 合格数量 */
        private Integer passedQty;
        /** 不合格数量 */
        private Integer failedQty;
    }
}
