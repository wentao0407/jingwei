package com.jingwei.order.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.domain.model.UserContext;
import com.jingwei.order.application.dto.ReturnOrderCreateDTO;
import com.jingwei.order.application.dto.ReturnOrderQueryDTO;
import com.jingwei.order.application.dto.ReturnQcDTO;
import com.jingwei.order.domain.model.*;
import com.jingwei.order.domain.service.ReturnOrderDomainService;
import com.jingwei.order.domain.service.ReturnOrderDomainService.QcResultItem;
import com.jingwei.order.interfaces.vo.ReturnOrderLineVO;
import com.jingwei.order.interfaces.vo.ReturnOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 退货管理应用服务 — 编排层
 * <p>
 * 负责退货单的查询和用户操作编排。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnOrderApplicationService {

    private final ReturnOrderDomainService returnOrderDomainService;
    private final ObjectMapper objectMapper;

    /**
     * 退货类型标签映射
     * <p>
     * 枚举编码 → 中文标签，用于 VO 层展示。
     * 例如：CUSTOMER_REJECT → "客户退货"
     * </p>
     */
    private static final Map<String, String> RETURN_TYPE_LABELS = Arrays.stream(ReturnType.values())
            .collect(Collectors.toMap(ReturnType::name, ReturnType::getLabel));

    /**
     * 退货状态标签映射
     * <p>
     * 枚举编码 → 中文标签，用于 VO 层展示。
     * 例如：DRAFT → "草稿"，PENDING_APPROVAL → "待审批"
     * </p>
     */
    private static final Map<String, String> STATUS_LABELS = Arrays.stream(ReturnStatus.values())
            .collect(Collectors.toMap(ReturnStatus::name, ReturnStatus::getLabel));

    /**
     * 创建退货单
     *
     * @param dto 创建退货单 DTO
     * @return 退货单 VO
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnOrderVO createReturnOrder(ReturnOrderCreateDTO dto) {
        // 转换为领域对象
        ReturnOrder returnOrder = new ReturnOrder();
        returnOrder.setReturnType(ReturnType.valueOf(dto.getReturnType()));
        returnOrder.setSalesOrderId(dto.getSalesOrderId());
        returnOrder.setSalesOrderNo(dto.getSalesOrderNo());
        returnOrder.setCustomerId(dto.getCustomerId());
        returnOrder.setReason(dto.getReason());
        returnOrder.setRemark(dto.getRemark());

        List<ReturnOrderLine> lines = dto.getLines().stream()
                .map(this::toLine)
                .toList();

        ReturnOrder created = returnOrderDomainService.createReturnOrder(returnOrder, lines);
        return toVO(created);
    }

    /**
     * 提交退货审批
     *
     * @param returnId 退货单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitForApproval(Long returnId) {
        Long userId = UserContext.getUserId();
        returnOrderDomainService.submitForApproval(returnId, userId);
    }

    /**
     * 审批通过
     *
     * @param returnId 退货单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long returnId) {
        Long userId = UserContext.getUserId();
        returnOrderDomainService.approve(returnId, userId);
    }

    /**
     * 审批驳回
     *
     * @param returnId 退货单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long returnId) {
        returnOrderDomainService.reject(returnId);
    }

    /**
     * 退货收货确认
     *
     * @param returnId 退货单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long returnId) {
        returnOrderDomainService.confirmReceive(returnId);
    }

    /**
     * 退货质检
     *
     * @param dto 质检 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void processQc(ReturnQcDTO dto) {
        List<QcResultItem> qcResults = dto.getResults().stream()
                .map(r -> {
                    QcResultItem item = new QcResultItem();
                    item.setLineId(r.getLineId());
                    item.setPassedQty(r.getPassedQty());
                    item.setFailedQty(r.getFailedQty());
                    return item;
                })
                .toList();

        returnOrderDomainService.processQc(dto.getReturnId(), qcResults);
    }

    /**
     * 查询退货单详情
     *
     * @param returnId 退货单ID
     * @return 退货单 VO
     */
    public ReturnOrderVO getDetail(Long returnId) {
        ReturnOrder returnOrder = returnOrderDomainService.getDetail(returnId);
        return toVO(returnOrder);
    }

    // ==================== 私有方法 ====================

    /**
     * DTO → 退货行领域对象
     */
    private ReturnOrderLine toLine(ReturnOrderCreateDTO.ReturnLineDTO dto) {
        ReturnOrderLine line = new ReturnOrderLine();
        line.setSalesOrderLineId(dto.getSalesOrderLineId());
        line.setSpuId(dto.getSpuId());
        line.setColorWayId(dto.getColorWayId());
        line.setTotalQuantity(dto.getTotalQuantity());
        line.setQcPassedQty(0);
        line.setQcFailedQty(0);
        line.setRemark(dto.getRemark());

        // 解析尺码矩阵 JSON
        try {
            SizeMatrix sizeMatrix = objectMapper.readValue(dto.getSizeMatrixJson(), SizeMatrix.class);
            line.setSizeMatrix(sizeMatrix);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.PARAM_VALIDATION_FAILED, "尺码矩阵格式不正确");
        }

        return line;
    }

    /**
     * 退货单 → VO
     */
    private ReturnOrderVO toVO(ReturnOrder returnOrder) {
        ReturnOrderVO vo = new ReturnOrderVO();
        vo.setId(returnOrder.getId());
        vo.setReturnNo(returnOrder.getReturnNo());
        vo.setReturnType(returnOrder.getReturnType().name());
        vo.setReturnTypeLabel(RETURN_TYPE_LABELS.getOrDefault(
                returnOrder.getReturnType().name(), returnOrder.getReturnType().name()));
        vo.setSalesOrderId(returnOrder.getSalesOrderId());
        vo.setSalesOrderNo(returnOrder.getSalesOrderNo());
        vo.setCustomerId(returnOrder.getCustomerId());
        vo.setReason(returnOrder.getReason());
        vo.setStatus(returnOrder.getStatus().name());
        vo.setStatusLabel(STATUS_LABELS.getOrDefault(
                returnOrder.getStatus().name(), returnOrder.getStatus().name()));
        vo.setTotalQuantity(returnOrder.getTotalQuantity());
        vo.setInboundOrderId(returnOrder.getInboundOrderId());
        vo.setApprovedBy(returnOrder.getApprovedBy());
        vo.setApprovedAt(returnOrder.getApprovedAt());
        vo.setRemark(returnOrder.getRemark());
        vo.setCreatedAt(returnOrder.getCreatedAt());

        // 退货行
        if (returnOrder.getLines() != null && !returnOrder.getLines().isEmpty()) {
            vo.setLines(returnOrder.getLines().stream()
                    .map(this::toLineVO)
                    .toList());
        }

        return vo;
    }

    /**
     * 退货行 → VO
     */
    private ReturnOrderLineVO toLineVO(ReturnOrderLine line) {
        ReturnOrderLineVO vo = new ReturnOrderLineVO();
        vo.setId(line.getId());
        vo.setReturnId(line.getReturnId());
        vo.setSalesOrderLineId(line.getSalesOrderLineId());
        vo.setSpuId(line.getSpuId());
        vo.setColorWayId(line.getColorWayId());
        vo.setTotalQuantity(line.getTotalQuantity());
        vo.setQcPassedQty(line.getQcPassedQty());
        vo.setQcFailedQty(line.getQcFailedQty());
        vo.setQcResult(line.getQcResult());
        vo.setRemark(line.getRemark());

        // 序列化尺码矩阵
        if (line.getSizeMatrix() != null) {
            try {
                vo.setSizeMatrixJson(objectMapper.writeValueAsString(line.getSizeMatrix()));
            } catch (JsonProcessingException e) {
                log.warn("尺码矩阵序列化失败: lineId={}", line.getId());
            }
        }

        return vo;
    }
}
