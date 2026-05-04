package com.jingwei.procurement.domain.service;

import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.procurement.domain.model.*;
import com.jingwei.procurement.domain.repository.AsnLineRepository;
import com.jingwei.procurement.domain.repository.AsnRepository;
import com.jingwei.procurement.domain.repository.ProcurementOrderLineRepository;
import com.jingwei.procurement.domain.repository.ProcurementOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 到货通知单领域服务
 * <p>
 * 负责到货通知单的创建、收货确认和检验结果提交。
 * 收货时更新采购订单行的已到货数量，检验后更新合格/不合格数量。
 * 库存变更通过 {@link InventoryChangeService} 预留接口处理。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsnDomainService {

    private static final String ASN_CODE_RULE = "ASN";

    private final AsnRepository asnRepository;
    private final AsnLineRepository asnLineRepository;
    private final ProcurementOrderRepository procurementOrderRepository;
    private final ProcurementOrderLineRepository procurementOrderLineRepository;
    private final CodingRuleDomainService codingRuleDomainService;
    private final InventoryChangeService inventoryChangeService;

    /**
     * 创建到货通知单
     *
     * @param asn   到货通知单（含行列表）
     * @return 保存后的到货通知单
     */
    @Transactional(rollbackFor = Exception.class)
    public Asn createAsn(Asn asn) {
        String asnNo = codingRuleDomainService.generateCode(ASN_CODE_RULE, Collections.emptyMap());
        asn.setAsnNo(asnNo);
        asn.setStatus(AsnStatus.PENDING);

        // 保存主表
        asnRepository.insert(asn);

        // 保存行并校验到货数量不超过采购订单行剩余可收数量
        for (AsnLine line : asn.getLines()) {
            line.setAsnId(asn.getId());
            line.setQcStatus(QcStatus.PENDING);
            line.setReceivedQuantity(BigDecimal.ZERO);
            line.setAcceptedQuantity(BigDecimal.ZERO);
            line.setRejectedQuantity(BigDecimal.ZERO);

            // 校验：到货通知数量不能超过采购订单行剩余可收数量
            if (line.getProcurementLineId() != null) {
                ProcurementOrderLine orderLine = procurementOrderLineRepository.selectById(line.getProcurementLineId());
                if (orderLine != null) {
                    BigDecimal remaining = orderLine.getQuantity()
                            .subtract(orderLine.getDeliveredQuantity() != null ? orderLine.getDeliveredQuantity() : BigDecimal.ZERO);
                    if (line.getExpectedQuantity().compareTo(remaining) > 0) {
                        throw new BizException(ErrorCode.ASN_QUANTITY_EXCEEDED,
                                "到货通知数量 " + line.getExpectedQuantity() + " 超过剩余可收数量 " + remaining);
                    }
                }
            }

            asnLineRepository.insert(line);
        }

        log.info("创建到货通知单: id={}, asnNo={}, procurementOrderId={}", asn.getId(), asnNo, asn.getProcurementOrderId());
        return asn;
    }

    /**
     * 确认收货
     * <p>
     * 记录各物料实收数量，更新采购订单行已到货数量。
     * 收货后触发库存变更：在途库存减少，质检库存增加。
     * </p>
     *
     * @param asnId          到货通知单ID
     * @param receivedLines  各行实收信息（lineId → 实收数量）
     * @param receiverId     收货人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void receiveGoods(Long asnId, List<ReceiveLine> receivedLines, Long receiverId) {
        Asn asn = asnRepository.selectById(asnId);
        if (asn == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND);
        }
        if (asn.getStatus() != AsnStatus.PENDING && asn.getStatus() != AsnStatus.PARTIAL_RECEIVED) {
            throw new BizException(ErrorCode.ASN_STATUS_INVALID, "当前状态不允许收货");
        }

        for (ReceiveLine rl : receivedLines) {
            AsnLine line = asnLineRepository.selectById(rl.lineId());
            if (line == null || !line.getAsnId().equals(asnId)) {
                throw new BizException(ErrorCode.DATA_NOT_FOUND, "到货行不存在");
            }

            // 记录实收数量
            line.setReceivedQuantity(rl.receivedQuantity());
            asnLineRepository.updateById(line);

            // 更新采购订单行已到货数量
            if (line.getProcurementLineId() != null) {
                ProcurementOrderLine orderLine = procurementOrderLineRepository.selectById(line.getProcurementLineId());
                if (orderLine != null) {
                    BigDecimal newDelivered = orderLine.getDeliveredQuantity().add(rl.receivedQuantity());
                    orderLine.setDeliveredQuantity(newDelivered);
                    procurementOrderLineRepository.updateById(orderLine);
                }
            }

            // 触发库存变更：在途 → 质检
            if (line.getMaterialId() != null && rl.receivedQuantity().compareTo(BigDecimal.ZERO) > 0) {
                inventoryChangeService.inTransitToQc(line.getMaterialId(), rl.receivedQuantity());
            }
        }

        // 更新ASN状态：判断是否全部到货
        asn.setReceiverId(receiverId);
        asn.setActualArrivalDate(LocalDate.now());

        // 查询所有行，判断是否全部到货
        List<AsnLine> allLines = asnLineRepository.selectByAsnId(asnId);
        boolean allReceived = allLines.stream().allMatch(l ->
                l.getReceivedQuantity() != null
                        && l.getReceivedQuantity().compareTo(l.getExpectedQuantity()) >= 0);
        asn.setStatus(allReceived ? AsnStatus.RECEIVED : AsnStatus.PARTIAL_RECEIVED);
        asnRepository.updateById(asn);

        log.info("到货通知单收货完成: asnId={}, receiverId={}", asnId, receiverId);
    }

    /**
     * 提交检验结果
     * <p>
     * 根据检验结果更新采购订单行的合格/不合格数量：
     * <ul>
     *   <li>PASSED → 质检库存转可用库存</li>
     *   <li>FAILED → 质检库存出库（退货）</li>
     *   <li>CONCESSION → 质检库存转可用库存（标记降级）</li>
     * </ul>
     * </p>
     *
     * @param lineId   到货行ID
     * @param qcResult 检验结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitQcResult(Long lineId, QcResult qcResult) {
        AsnLine line = asnLineRepository.selectById(lineId);
        if (line == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND);
        }
        if (line.getQcStatus() != QcStatus.PENDING) {
            throw new BizException(ErrorCode.QC_ALREADY_COMPLETED);
        }

        // 校验：合格 + 不合格 = 实收数量
        BigDecimal accepted = line.getAcceptedQuantity() != null ? line.getAcceptedQuantity() : BigDecimal.ZERO;
        BigDecimal rejected = line.getRejectedQuantity() != null ? line.getRejectedQuantity() : BigDecimal.ZERO;
        if (accepted.add(rejected).compareTo(line.getReceivedQuantity()) != 0) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED,
                    "合格数量(" + accepted + ") + 不合格数量(" + rejected + ") 必须等于实收数量(" + line.getReceivedQuantity() + ")");
        }

        // 确定检验状态
        QcStatus qcStatus;
        if (rejected.compareTo(BigDecimal.ZERO) == 0) {
            qcStatus = QcStatus.PASSED;
        } else if (accepted.compareTo(BigDecimal.ZERO) == 0) {
            qcStatus = QcStatus.FAILED;
        } else {
            // 部分合格视为让步接收
            qcStatus = QcStatus.CONCESSION;
        }

        line.setQcStatus(qcStatus);
        line.setQcResult(qcResult);
        asnLineRepository.updateById(line);

        // 更新采购订单行的合格/不合格数量
        if (line.getProcurementLineId() != null) {
            ProcurementOrderLine orderLine = procurementOrderLineRepository.selectById(line.getProcurementLineId());
            if (orderLine != null) {
                orderLine.setAcceptedQuantity(orderLine.getAcceptedQuantity().add(accepted));
                orderLine.setRejectedQuantity(orderLine.getRejectedQuantity().add(rejected));
                procurementOrderLineRepository.updateById(orderLine);
            }
        }

        // 触发库存变更
        if (line.getMaterialId() != null) {
            if (qcStatus == QcStatus.PASSED || qcStatus == QcStatus.CONCESSION) {
                // 合格或让步接收 → 质检转可用
                inventoryChangeService.qcToAvailable(line.getMaterialId(), accepted);
            }
            if (qcStatus == QcStatus.FAILED) {
                // 不合格 → 质检出库（退货）
                inventoryChangeService.qcOut(line.getMaterialId(), rejected);
            }
            if (qcStatus == QcStatus.CONCESSION && rejected.compareTo(BigDecimal.ZERO) > 0) {
                // 让步接收中不合格部分 → 退货
                inventoryChangeService.qcOut(line.getMaterialId(), rejected);
            }
        }

        log.info("检验结果提交: lineId={}, qcStatus={}, accepted={}, rejected={}", lineId, qcStatus, accepted, rejected);
    }

    /**
     * 查询到货通知单详情
     */
    public Asn getAsnDetail(Long asnId) {
        Asn asn = asnRepository.selectDetailById(asnId);
        if (asn == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND);
        }
        return asn;
    }

    /**
     * 收货行数据
     *
     * @param lineId          到货行ID
     * @param receivedQuantity 实收数量
     */
    public record ReceiveLine(Long lineId, BigDecimal receivedQuantity) {
    }
}
