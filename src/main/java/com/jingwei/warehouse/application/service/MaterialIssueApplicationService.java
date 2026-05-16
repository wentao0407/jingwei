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
import com.jingwei.warehouse.application.dto.CreateMaterialIssueDTO;
import com.jingwei.warehouse.application.dto.MaterialIssueQueryDTO;
import com.jingwei.warehouse.domain.model.MaterialIssueLine;
import com.jingwei.warehouse.domain.model.MaterialIssueOrder;
import com.jingwei.warehouse.domain.model.MaterialIssueStatus;
import com.jingwei.warehouse.domain.repository.MaterialIssueRepository;
import com.jingwei.warehouse.interfaces.vo.MaterialIssueVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 领料单应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialIssueApplicationService {

    private static final String ISSUE_NO_RULE = "MATERIAL_ISSUE_NO";

    private final MaterialIssueRepository materialIssueRepository;
    private final InventoryDomainService inventoryDomainService;
    private final InventoryMaterialRepository inventoryMaterialRepository;
    private final MaterialRepository materialRepository;
    private final CodingRuleDomainService codingRuleDomainService;

    /**
     * 创建领料单
     */
    @Transactional(rollbackFor = Exception.class)
    public MaterialIssueVO createIssue(CreateMaterialIssueDTO dto) {
        String issueNo = codingRuleDomainService.generateCode(ISSUE_NO_RULE, Collections.emptyMap());

        MaterialIssueOrder order = new MaterialIssueOrder();
        order.setIssueNo(issueNo);
        order.setProductionOrderId(dto.getProductionOrderId());
        order.setProductionLineId(dto.getProductionLineId());
        order.setStatus(MaterialIssueStatus.DRAFT);
        order.setRemark(dto.getRemark());
        materialIssueRepository.insert(order);

        List<MaterialIssueLine> lines = new ArrayList<>();
        for (CreateMaterialIssueDTO.MaterialIssueLineDTO lineDTO : dto.getLines()) {
            MaterialIssueLine line = new MaterialIssueLine();
            line.setIssueId(order.getId());
            line.setMaterialId(lineDTO.getMaterialId());
            line.setBatchNo(lineDTO.getBatchNo());
            line.setQuantity(lineDTO.getQuantity());
            line.setUnit(lineDTO.getUnit());
            line.setRemark(lineDTO.getRemark());
            lines.add(line);
        }
        order.setLines(lines);

        log.info("创建领料单: id={}, issueNo={}", order.getId(), issueNo);
        return toVO(order);
    }

    /**
     * 确认领料 — 扣减原料可用库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmIssue(Long issueId) {
        MaterialIssueOrder order = materialIssueRepository.selectDetailById(issueId);
        if (order == null) throw new BizException(ErrorCode.DATA_NOT_FOUND);
        if (order.getStatus() != MaterialIssueStatus.DRAFT) {
            throw new BizException(ErrorCode.ORDER_STATE_TRANSITION_INVALID, "仅草稿状态可确认");
        }

        Long operatorId = UserContext.getUserId();
        // 需要知道从哪个仓库领料，从第一行的库存记录获取仓库ID
        // 实际项目中建议在领料单头信息中存储 warehouseId
        Long warehouseId = null;
        for (MaterialIssueLine line : order.getLines()) {
            List<InventoryMaterial> mats = inventoryMaterialRepository.selectByMaterialId(line.getMaterialId());
            if (mats == null || mats.isEmpty()) {
                throw new BizException(ErrorCode.INVENTORY_NOT_FOUND, "物料库存记录不存在: materialId=" + line.getMaterialId());
            }
            // 使用第一个有可用库存的记录
            InventoryMaterial mat = mats.stream()
                    .filter(m -> m.getAvailableQty() != null && m.getAvailableQty().compareTo(line.getQuantity()) >= 0)
                    .findFirst()
                    .orElse(mats.get(0));
            if (warehouseId == null) warehouseId = mat.getWarehouseId();

            ChangeInventoryCommand cmd = new ChangeInventoryCommand();
            cmd.setOperationType(OperationType.OUTBOUND_MATERIAL);
            cmd.setInventoryType(InventoryType.MATERIAL);
            cmd.setInventoryId(mat.getId());
            cmd.setMaterialId(line.getMaterialId());
            cmd.setWarehouseId(mat.getWarehouseId());
            cmd.setQuantity(line.getQuantity());
            cmd.setOperatorId(operatorId);
            cmd.setSourceType("MATERIAL_ISSUE");
            cmd.setSourceId(order.getId());
            cmd.setSourceNo(order.getIssueNo());
            inventoryDomainService.changeInventory(cmd);
        }

        order.setStatus(MaterialIssueStatus.CONFIRMED);
        materialIssueRepository.updateById(order);
        log.info("确认领料单: id={}, operatorId={}", issueId, operatorId);
    }

    public MaterialIssueVO getDetail(Long issueId) {
        MaterialIssueOrder order = materialIssueRepository.selectDetailById(issueId);
        if (order == null) throw new BizException(ErrorCode.DATA_NOT_FOUND);
        return toVO(order);
    }

    public IPage<MaterialIssueVO> pageQuery(MaterialIssueQueryDTO dto) {
        Page<MaterialIssueOrder> page = new Page<>(dto.getCurrent(), dto.getSize());
        MaterialIssueStatus status = dto.getStatus() != null ? MaterialIssueStatus.valueOf(dto.getStatus()) : null;
        return materialIssueRepository.selectPage(page, status).convert(this::toVO);
    }

    private MaterialIssueVO toVO(MaterialIssueOrder order) {
        MaterialIssueVO vo = new MaterialIssueVO();
        vo.setId(order.getId());
        vo.setIssueNo(order.getIssueNo());
        vo.setProductionOrderId(order.getProductionOrderId());
        vo.setProductionLineId(order.getProductionLineId());
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

    private MaterialIssueVO.MaterialIssueLineVO toLineVO(MaterialIssueLine line) {
        MaterialIssueVO.MaterialIssueLineVO vo = new MaterialIssueVO.MaterialIssueLineVO();
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
