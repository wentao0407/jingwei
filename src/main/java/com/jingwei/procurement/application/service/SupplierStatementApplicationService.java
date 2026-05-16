package com.jingwei.procurement.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.procurement.application.dto.GenerateStatementDTO;
import com.jingwei.procurement.application.dto.StatementQueryDTO;
import com.jingwei.procurement.domain.model.*;
import com.jingwei.procurement.domain.repository.*;
import com.jingwei.procurement.infrastructure.persistence.AsnLineMapper;
import com.jingwei.procurement.infrastructure.persistence.AsnMapper;
import com.jingwei.procurement.infrastructure.persistence.ProcurementOrderLineMapper;
import com.jingwei.procurement.interfaces.vo.StatementLineVO;
import com.jingwei.procurement.interfaces.vo.StatementVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 供应商对账单应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierStatementApplicationService {

    private static final String STATEMENT_NO_RULE = "STATEMENT_NO";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final SupplierStatementRepository statementRepository;
    private final SupplierStatementLineRepository statementLineRepository;
    private final CodingRuleDomainService codingRuleDomainService;
    private final AsnMapper asnMapper;
    private final AsnLineMapper asnLineMapper;
    private final ProcurementOrderLineMapper procurementOrderLineMapper;

    /**
     * 生成对账单：查询期间内该供应商的 ASN 检验合格记录，按物料汇总金额。
     */
    @Transactional(rollbackFor = Exception.class)
    public StatementVO generateStatement(GenerateStatementDTO dto) {
        // 1. 查询该供应商在对账期间内已收货/已关闭的 ASN
        List<Asn> asns = asnMapper.selectList(
                new LambdaQueryWrapper<Asn>()
                        .eq(Asn::getSupplierId, dto.getSupplierId())
                        .in(Asn::getStatus, AsnStatus.RECEIVED, AsnStatus.CLOSED)
                        .ge(Asn::getActualArrivalDate, dto.getPeriodStart())
                        .le(Asn::getActualArrivalDate, dto.getPeriodEnd())
                        .eq(Asn::getDeleted, false));

        if (asns.isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "该期间内无已收货的到货记录");
        }

        List<Long> asnIds = asns.stream().map(Asn::getId).collect(Collectors.toList());

        // 2. 查询这些 ASN 的合格行（PASSED 或 CONCESSION，acceptedQuantity > 0）
        List<AsnLine> qualifiedLines = asnLineMapper.selectList(
                new LambdaQueryWrapper<AsnLine>()
                        .in(AsnLine::getAsnId, asnIds)
                        .in(AsnLine::getQcStatus, QcStatus.PASSED, QcStatus.CONCESSION)
                        .gt(AsnLine::getAcceptedQuantity, BigDecimal.ZERO)
                        .eq(AsnLine::getDeleted, false));

        if (qualifiedLines.isEmpty()) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "该期间内无检验合格的到货行");
        }

        // 3. 获取关联的采购订单行，用于取单价
        Set<Long> procurementLineIds = qualifiedLines.stream()
                .map(AsnLine::getProcurementLineId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, BigDecimal> unitPriceMap = new HashMap<>();
        Map<Long, Long> orderLineToOrderMap = new HashMap<>();
        if (!procurementLineIds.isEmpty()) {
            List<ProcurementOrderLine> orderLines = procurementOrderLineMapper.selectBatchIds(procurementLineIds);
            for (ProcurementOrderLine ol : orderLines) {
                unitPriceMap.put(ol.getId(), ol.getUnitPrice() != null ? ol.getUnitPrice() : BigDecimal.ZERO);
                orderLineToOrderMap.put(ol.getId(), ol.getOrderId());
            }
        }

        // 4. 建立 asnId -> asn 映射
        Map<Long, Asn> asnMap = asns.stream().collect(Collectors.toMap(Asn::getId, a -> a));

        // 5. 按物料ID汇总（合并同物料的多条记录）
        // key: materialId, value: { totalQty, unitPrice (取第一条), asnIds, orderIds }
        Map<Long, MaterialAggregation> aggregationMap = new LinkedHashMap<>();
        for (AsnLine line : qualifiedLines) {
            MaterialAggregation agg = aggregationMap.computeIfAbsent(line.getMaterialId(),
                    k -> new MaterialAggregation());
            agg.totalQuantity = agg.totalQuantity.add(line.getAcceptedQuantity());
            agg.asnIds.add(line.getAsnId());
            Long orderId = orderLineToOrderMap.get(line.getProcurementLineId());
            if (orderId != null) {
                agg.orderIds.add(orderId);
            }
            // 取第一条的单价（同物料通常单价一致）
            if (agg.unitPrice == null) {
                agg.unitPrice = unitPriceMap.getOrDefault(line.getProcurementLineId(), BigDecimal.ZERO);
            }
        }

        // 6. 创建对账单
        String statementNo = codingRuleDomainService.generateCode(STATEMENT_NO_RULE, Collections.emptyMap());
        SupplierStatement statement = new SupplierStatement();
        statement.setStatementNo(statementNo);
        statement.setSupplierId(dto.getSupplierId());
        statement.setPeriodStart(dto.getPeriodStart());
        statement.setPeriodEnd(dto.getPeriodEnd());
        statement.setStatus(StatementStatus.DRAFT);
        statement.setRemark(dto.getRemark());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SupplierStatementLine> lines = new ArrayList<>();

        for (Map.Entry<Long, MaterialAggregation> entry : aggregationMap.entrySet()) {
            MaterialAggregation agg = entry.getValue();
            BigDecimal lineAmount = agg.totalQuantity.multiply(agg.unitPrice);

            SupplierStatementLine line = new SupplierStatementLine();
            line.setMaterialId(entry.getKey());
            line.setAcceptedQuantity(agg.totalQuantity);
            line.setUnitPrice(agg.unitPrice);
            line.setLineAmount(lineAmount);
            // 取第一个 ASN 和采购订单作为来源
            if (!agg.asnIds.isEmpty()) {
                line.setAsnId(agg.asnIds.iterator().next());
            }
            if (!agg.orderIds.isEmpty()) {
                line.setProcurementOrderId(agg.orderIds.iterator().next());
            }
            lines.add(line);
            totalAmount = totalAmount.add(lineAmount);
        }

        statement.setTotalAmount(totalAmount);
        statementRepository.insert(statement);

        for (SupplierStatementLine line : lines) {
            line.setStatementId(statement.getId());
            statementLineRepository.insert(line);
        }

        log.info("生成对账单: statementNo={}, supplierId={}, lines={}, totalAmount={}",
                statementNo, dto.getSupplierId(), lines.size(), totalAmount);

        return buildStatementVO(statement, lines);
    }

    /**
     * 确认对账单
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmStatement(Long statementId) {
        SupplierStatement statement = statementRepository.selectById(statementId);
        if (statement == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "对账单不存在");
        }
        if (statement.getStatus() != StatementStatus.DRAFT) {
            throw new BizException(ErrorCode.ORDER_STATE_TRANSITION_INVALID, "只有草稿状态的对账单才能确认");
        }
        statement.setStatus(StatementStatus.CONFIRMED);
        statementRepository.updateById(statement);
        log.info("确认对账单: id={}", statementId);
    }

    /**
     * 标记争议
     */
    @Transactional(rollbackFor = Exception.class)
    public void disputeStatement(Long statementId) {
        SupplierStatement statement = statementRepository.selectById(statementId);
        if (statement == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "对账单不存在");
        }
        if (statement.getStatus() == StatementStatus.DISPUTED) {
            throw new BizException(ErrorCode.ORDER_STATE_TRANSITION_INVALID, "对账单已处于争议状态");
        }
        statement.setStatus(StatementStatus.DISPUTED);
        statementRepository.updateById(statement);
        log.info("标记争议对账单: id={}", statementId);
    }

    /**
     * 查询对账单详情
     */
    public StatementVO getDetail(Long statementId) {
        SupplierStatement statement = statementRepository.selectDetailById(statementId);
        if (statement == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "对账单不存在");
        }
        return buildStatementVO(statement, statement.getLines());
    }

    /**
     * 分页查询对账单
     */
    public IPage<StatementVO> pageQuery(StatementQueryDTO dto) {
        Page<SupplierStatement> page = new Page<>(
                Math.max(1, dto.getCurrent()), Math.max(1, dto.getSize()));
        StatementStatus status = null;
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            status = StatementStatus.valueOf(dto.getStatus());
        }
        IPage<SupplierStatement> result = statementRepository.selectPage(page, dto.getSupplierId(), status);
        return result.convert(s -> buildSummaryVO(s));
    }

    private StatementVO buildStatementVO(SupplierStatement statement, List<SupplierStatementLine> lines) {
        StatementVO vo = buildSummaryVO(statement);
        if (lines != null) {
            vo.setLines(lines.stream().map(this::buildLineVO).collect(Collectors.toList()));
        }
        return vo;
    }

    private StatementVO buildSummaryVO(SupplierStatement statement) {
        StatementVO vo = new StatementVO();
        vo.setId(statement.getId());
        vo.setStatementNo(statement.getStatementNo());
        vo.setSupplierId(statement.getSupplierId());
        vo.setPeriodStart(statement.getPeriodStart() != null ? statement.getPeriodStart().format(DATE_FMT) : null);
        vo.setPeriodEnd(statement.getPeriodEnd() != null ? statement.getPeriodEnd().format(DATE_FMT) : null);
        vo.setTotalAmount(statement.getTotalAmount());
        vo.setStatus(statement.getStatus() != null ? statement.getStatus().name() : null);
        vo.setStatusLabel(statement.getStatus() != null ? statement.getStatus().getLabel() : null);
        vo.setRemark(statement.getRemark());
        vo.setCreatedAt(statement.getCreatedAt());
        vo.setUpdatedAt(statement.getUpdatedAt());
        return vo;
    }

    private StatementLineVO buildLineVO(SupplierStatementLine line) {
        StatementLineVO vo = new StatementLineVO();
        vo.setId(line.getId());
        vo.setStatementId(line.getStatementId());
        vo.setAsnId(line.getAsnId());
        vo.setProcurementOrderId(line.getProcurementOrderId());
        vo.setMaterialId(line.getMaterialId());
        vo.setAcceptedQuantity(line.getAcceptedQuantity());
        vo.setUnitPrice(line.getUnitPrice());
        vo.setLineAmount(line.getLineAmount());
        return vo;
    }

    /** 按物料汇总的内部辅助结构 */
    private static class MaterialAggregation {
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal unitPrice;
        Set<Long> asnIds = new LinkedHashSet<>();
        Set<Long> orderIds = new LinkedHashSet<>();
    }
}
