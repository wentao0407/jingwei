package com.jingwei.procurement.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.UserContext;
import com.jingwei.master.domain.model.Material;
import com.jingwei.master.domain.model.Supplier;
import com.jingwei.master.domain.repository.MaterialRepository;
import com.jingwei.master.domain.repository.SupplierRepository;
import com.jingwei.procurement.application.dto.AsnQueryDTO;
import com.jingwei.procurement.application.dto.CreateAsnDTO;
import com.jingwei.procurement.application.dto.ReceiveGoodsDTO;
import com.jingwei.procurement.application.dto.SubmitQcResultDTO;
import com.jingwei.procurement.domain.model.*;
import com.jingwei.procurement.domain.repository.AsnLineRepository;
import com.jingwei.procurement.domain.repository.AsnRepository;
import com.jingwei.procurement.domain.service.AsnDomainService;
import com.jingwei.procurement.interfaces.vo.AsnLineVO;
import com.jingwei.procurement.interfaces.vo.AsnVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 到货通知单应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsnApplicationService {

    private final AsnDomainService asnDomainService;
    private final AsnRepository asnRepository;
    private final AsnLineRepository asnLineRepository;
    private final SupplierRepository supplierRepository;
    private final MaterialRepository materialRepository;

    /**
     * 创建到货通知单
     */
    @Transactional(rollbackFor = Exception.class)
    public AsnVO createAsn(CreateAsnDTO dto) {
        Asn asn = new Asn();
        asn.setProcurementOrderId(dto.getProcurementOrderId());
        asn.setSupplierId(dto.getSupplierId());
        asn.setExpectedArrivalDate(dto.getExpectedArrivalDate() != null
                ? LocalDate.parse(dto.getExpectedArrivalDate()) : null);
        asn.setRemark(dto.getRemark());

        List<AsnLine> lines = new ArrayList<>();
        for (CreateAsnDTO.AsnLineCreateDTO lineDto : dto.getLines()) {
            AsnLine line = new AsnLine();
            line.setProcurementLineId(lineDto.getProcurementLineId());
            line.setMaterialId(lineDto.getMaterialId());
            line.setExpectedQuantity(lineDto.getExpectedQuantity());
            line.setBatchNo(lineDto.getBatchNo());
            line.setRemark(lineDto.getRemark());
            lines.add(line);
        }
        asn.setLines(lines);

        Asn saved = asnDomainService.createAsn(asn);
        return toAsnVO(saved);
    }

    /**
     * 确认收货
     */
    @Transactional(rollbackFor = Exception.class)
    public void receiveGoods(ReceiveGoodsDTO dto) {
        Long receiverId = UserContext.getUserId();
        List<AsnDomainService.ReceiveLine> receiveLines = dto.getLines().stream()
                .map(l -> new AsnDomainService.ReceiveLine(l.getLineId(), l.getReceivedQuantity()))
                .toList();
        asnDomainService.receiveGoods(dto.getAsnId(), receiveLines, receiverId);
    }

    /**
     * 提交检验结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitQcResult(SubmitQcResultDTO dto) {
        // 先更新行的合格/不合格数量
        AsnLine line = asnLineRepository.selectById(dto.getLineId());
        line.setAcceptedQuantity(dto.getAcceptedQuantity());
        line.setRejectedQuantity(dto.getRejectedQuantity());
        asnLineRepository.updateById(line);

        // 构建检验结果值对象
        QcResult qcResult = new QcResult();
        qcResult.setInspector(dto.getInspector());
        qcResult.setInspectedAt(LocalDateTime.now());
        qcResult.setOverallResult(dto.getAcceptedQuantity().compareTo(BigDecimal.ZERO) == 0 ? "FAIL" : "PASS");
        qcResult.setConclusion(dto.getConclusion());

        if (dto.getItems() != null) {
            qcResult.setItems(dto.getItems().stream().map(item -> {
                QcResult.QcItem qi = new QcResult.QcItem();
                qi.setName(item.getName());
                qi.setStandard(item.getStandard());
                qi.setActual(item.getActual());
                qi.setResult(item.getResult());
                return qi;
            }).toList());
        }

        asnDomainService.submitQcResult(dto.getLineId(), qcResult);
    }

    /**
     * 查询详情
     */
    public AsnVO getDetail(Long asnId) {
        Asn asn = asnDomainService.getAsnDetail(asnId);
        return toAsnVO(asn);
    }

    /**
     * 分页查询
     */
    public IPage<AsnVO> pageQuery(AsnQueryDTO dto) {
        Page<Asn> page = new Page<>(dto.getCurrent(), dto.getSize());
        AsnStatus status = dto.getStatus() != null ? AsnStatus.valueOf(dto.getStatus()) : null;

        IPage<Asn> asnPage = asnRepository.selectPage(page, dto.getProcurementOrderId(), status);
        return asnPage.convert(this::toAsnVO);
    }

    // ==================== 私有方法 ====================

    private AsnVO toAsnVO(Asn asn) {
        AsnVO vo = new AsnVO();
        vo.setId(asn.getId());
        vo.setAsnNo(asn.getAsnNo());
        vo.setProcurementOrderId(asn.getProcurementOrderId());
        vo.setSupplierId(asn.getSupplierId());
        vo.setExpectedArrivalDate(asn.getExpectedArrivalDate() != null ? asn.getExpectedArrivalDate().toString() : null);
        vo.setActualArrivalDate(asn.getActualArrivalDate() != null ? asn.getActualArrivalDate().toString() : null);
        vo.setStatus(asn.getStatus() != null ? asn.getStatus().name() : null);
        vo.setStatusLabel(asn.getStatus() != null ? asn.getStatus().getLabel() : null);
        vo.setReceiverId(asn.getReceiverId());
        vo.setRemark(asn.getRemark());
        vo.setCreatedAt(asn.getCreatedAt());
        vo.setUpdatedAt(asn.getUpdatedAt());

        // 供应商名称
        if (asn.getSupplierId() != null) {
            Supplier supplier = supplierRepository.selectById(asn.getSupplierId());
            if (supplier != null) {
                vo.setSupplierName(supplier.getName());
            }
        }

        if (asn.getLines() != null && !asn.getLines().isEmpty()) {
            vo.setLines(asn.getLines().stream().map(this::toLineVO).toList());
        } else {
            vo.setLines(List.of());
        }

        return vo;
    }

    private AsnLineVO toLineVO(AsnLine line) {
        AsnLineVO vo = new AsnLineVO();
        vo.setId(line.getId());
        vo.setAsnId(line.getAsnId());
        vo.setProcurementLineId(line.getProcurementLineId());
        vo.setMaterialId(line.getMaterialId());
        vo.setExpectedQuantity(line.getExpectedQuantity());
        vo.setReceivedQuantity(line.getReceivedQuantity());
        vo.setQcStatus(line.getQcStatus() != null ? line.getQcStatus().name() : null);
        vo.setQcStatusLabel(line.getQcStatus() != null ? line.getQcStatus().getLabel() : null);
        vo.setAcceptedQuantity(line.getAcceptedQuantity());
        vo.setRejectedQuantity(line.getRejectedQuantity());
        vo.setBatchNo(line.getBatchNo());
        vo.setRemark(line.getRemark());

        // 物料信息
        if (line.getMaterialId() != null) {
            Material material = materialRepository.selectById(line.getMaterialId());
            if (material != null) {
                vo.setMaterialCode(material.getCode());
                vo.setMaterialName(material.getName());
            }
        }

        // 检验结果
        if (line.getQcResult() != null) {
            AsnLineVO.QcResultVO qrVo = new AsnLineVO.QcResultVO();
            qrVo.setInspector(line.getQcResult().getInspector());
            qrVo.setInspectedAt(line.getQcResult().getInspectedAt() != null
                    ? line.getQcResult().getInspectedAt().toString() : null);
            qrVo.setOverallResult(line.getQcResult().getOverallResult());
            qrVo.setConclusion(line.getQcResult().getConclusion());
            if (line.getQcResult().getItems() != null) {
                qrVo.setItems(line.getQcResult().getItems().stream().map(item -> {
                    AsnLineVO.QcItemVO iv = new AsnLineVO.QcItemVO();
                    iv.setName(item.getName());
                    iv.setStandard(item.getStandard());
                    iv.setActual(item.getActual());
                    iv.setResult(item.getResult());
                    return iv;
                }).toList());
            }
            vo.setQcResult(qrVo);
        }

        return vo;
    }
}
