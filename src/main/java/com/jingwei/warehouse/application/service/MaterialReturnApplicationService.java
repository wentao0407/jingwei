package com.jingwei.warehouse.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.domain.model.UserContext;
import com.jingwei.inventory.domain.model.InventoryMaterial;
import com.jingwei.inventory.domain.model.InventoryType;
import com.jingwei.inventory.domain.model.OperationType;
import com.jingwei.inventory.domain.repository.InventoryMaterialRepository;
import com.jingwei.inventory.domain.service.ChangeInventoryCommand;
import com.jingwei.inventory.domain.service.InventoryDomainService;
import com.jingwei.master.domain.model.Material;
import com.jingwei.master.domain.repository.MaterialRepository;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.warehouse.application.dto.CreateMaterialReturnDTO;
import com.jingwei.warehouse.application.dto.MaterialReturnQueryDTO;
import com.jingwei.warehouse.domain.model.MaterialReturnLine;
import com.jingwei.warehouse.domain.model.MaterialReturnOrder;
import com.jingwei.warehouse.domain.model.MaterialReturnStatus;
import com.jingwei.warehouse.domain.repository.MaterialReturnRepository;
import com.jingwei.warehouse.interfaces.vo.MaterialReturnVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialReturnApplicationService {

    private static final String RETURN_NO_RULE = "MATERIAL_RETURN_NO";

    private final MaterialReturnRepository materialReturnRepository;
    private final InventoryDomainService inventoryDomainService;
    private final InventoryMaterialRepository inventoryMaterialRepository;
    private final MaterialRepository materialRepository;
    private final CodingRuleDomainService codingRuleDomainService;

    @Transactional(rollbackFor = Exception.class)
    public MaterialReturnVO createReturn(CreateMaterialReturnDTO dto) {
        String returnNo = codingRuleDomainService.generateCode(RETURN_NO_RULE, Collections.emptyMap());

        MaterialReturnOrder order = new MaterialReturnOrder();
        order.setReturnNo(returnNo);
        order.setProductionOrderId(dto.getProductionOrderId());
        order.setStatus(MaterialReturnStatus.DRAFT);
        order.setRemark(dto.getRemark());
        materialReturnRepository.insert(order);

        List<MaterialReturnLine> lines = new ArrayList<>();
        for (CreateMaterialReturnDTO.MaterialReturnLineDTO lineDTO : dto.getLines()) {
            MaterialReturnLine line = new MaterialReturnLine();
            line.setReturnId(order.getId());
            line.setMaterialId(lineDTO.getMaterialId());
            line.setBatchNo(lineDTO.getBatchNo());
            line.setQuantity(lineDTO.getQuantity());
            line.setUnit(lineDTO.getUnit());
            line.setRemark(lineDTO.getRemark());
            lines.add(line);
        }
        order.setLines(lines);

        log.info("创建退料单: id={}, returnNo={}", order.getId(), returnNo);
        return toVO(order);
    }

    /**
     * 确认退料 — 增加原料可用库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmReturn(Long returnId) {
        MaterialReturnOrder order = materialReturnRepository.selectDetailById(returnId);
        if (order == null) throw new BizException(ErrorCode.DATA_NOT_FOUND);
        if (order.getStatus() != MaterialReturnStatus.DRAFT) {
            throw new BizException(ErrorCode.ORDER_STATE_TRANSITION_INVALID, "仅草稿状态可确认");
        }

        Long operatorId = UserContext.getUserId();
        for (MaterialReturnLine line : order.getLines()) {
            List<InventoryMaterial> mats = inventoryMaterialRepository.selectByMaterialId(line.getMaterialId());
            if (mats == null || mats.isEmpty()) {
                throw new BizException(ErrorCode.INVENTORY_NOT_FOUND, "物料库存记录不存在: materialId=" + line.getMaterialId());
            }
            InventoryMaterial mat = mats.get(0);

            ChangeInventoryCommand cmd = new ChangeInventoryCommand();
            cmd.setOperationType(OperationType.INBOUND_RETURN);
            cmd.setInventoryType(InventoryType.MATERIAL);
            cmd.setInventoryId(mat.getId());
            cmd.setMaterialId(line.getMaterialId());
            cmd.setWarehouseId(mat.getWarehouseId());
            cmd.setQuantity(line.getQuantity());
            cmd.setOperatorId(operatorId);
            cmd.setSourceType("MATERIAL_RETURN");
            cmd.setSourceId(order.getId());
            cmd.setSourceNo(order.getReturnNo());
            inventoryDomainService.changeInventory(cmd);
        }

        order.setStatus(MaterialReturnStatus.CONFIRMED);
        materialReturnRepository.updateById(order);
        log.info("确认退料单: id={}", returnId);
    }

    public MaterialReturnVO getDetail(Long returnId) {
        MaterialReturnOrder order = materialReturnRepository.selectDetailById(returnId);
        if (order == null) throw new BizException(ErrorCode.DATA_NOT_FOUND);
        return toVO(order);
    }

    public IPage<MaterialReturnVO> pageQuery(MaterialReturnQueryDTO dto) {
        Page<MaterialReturnOrder> page = new Page<>(dto.getCurrent(), dto.getSize());
        MaterialReturnStatus status = dto.getStatus() != null ? MaterialReturnStatus.valueOf(dto.getStatus()) : null;
        return materialReturnRepository.selectPage(page, status).convert(this::toVO);
    }

    private MaterialReturnVO toVO(MaterialReturnOrder order) {
        MaterialReturnVO vo = new MaterialReturnVO();
        vo.setId(order.getId());
        vo.setReturnNo(order.getReturnNo());
        vo.setProductionOrderId(order.getProductionOrderId());
        vo.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        vo.setStatusLabel(order.getStatus() != null ? order.getStatus().getLabel() : null);
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());
        if (order.getLines() != null) {
            vo.setLines(order.getLines().stream().map(this::toLineVO).toList());
        } else {
            vo.setLines(List.of());
        }
        return vo;
    }

    private MaterialReturnVO.MaterialReturnLineVO toLineVO(MaterialReturnLine line) {
        MaterialReturnVO.MaterialReturnLineVO vo = new MaterialReturnVO.MaterialReturnLineVO();
        vo.setId(line.getId());
        vo.setMaterialId(line.getMaterialId());
        vo.setBatchNo(line.getBatchNo());
        vo.setQuantity(line.getQuantity());
        vo.setUnit(line.getUnit());
        vo.setRemark(line.getRemark());
        if (line.getMaterialId() != null) {
            Material mat = materialRepository.selectById(line.getMaterialId());
            if (mat != null) {
                vo.setMaterialCode(mat.getCode());
                vo.setMaterialName(mat.getName());
            }
        }
        return vo;
    }
}
