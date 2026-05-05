package com.jingwei.inventory.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.UserContext;
import com.jingwei.inventory.application.dto.CreateStocktakingDTO;
import com.jingwei.inventory.application.dto.RecordCountDTO;
import com.jingwei.inventory.application.dto.StocktakingQueryDTO;
import com.jingwei.inventory.domain.model.*;
import com.jingwei.inventory.domain.service.StocktakingDomainService;
import com.jingwei.inventory.domain.repository.StocktakingOrderRepository;
import com.jingwei.inventory.interfaces.vo.StocktakingLineVO;
import com.jingwei.inventory.interfaces.vo.StocktakingOrderVO;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 盘点单应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StocktakingApplicationService {

    /** 编码规则键：盘点单编号 */
    private static final String STOCKTAKING_NO_RULE = "STOCKTAKING_NO";

    private final StocktakingDomainService stocktakingDomainService;
    private final StocktakingOrderRepository stocktakingOrderRepository;
    private final CodingRuleDomainService codingRuleDomainService;

    /**
     * 创建盘点单
     */
    @Transactional(rollbackFor = Exception.class)
    public StocktakingOrderVO createStocktaking(CreateStocktakingDTO dto) {
        String stocktakingNo = codingRuleDomainService.generateCode(
                STOCKTAKING_NO_RULE, java.util.Collections.emptyMap());

        StocktakingOrder order = new StocktakingOrder();
        order.setStocktakingNo(stocktakingNo);
        order.setStocktakingType(StocktakingType.valueOf(dto.getStocktakingType()));
        order.setCountMode(CountMode.valueOf(dto.getCountMode()));
        order.setWarehouseId(dto.getWarehouseId());
        order.setZoneCode(dto.getZoneCode());
        order.setPlannedDate(dto.getPlannedDate() != null ? LocalDate.parse(dto.getPlannedDate()) : null);
        order.setRemark(dto.getRemark());

        StocktakingOrder saved = stocktakingDomainService.createOrder(order);
        return toVO(saved, false);
    }

    /**
     * 开始盘点
     */
    @Transactional(rollbackFor = Exception.class)
    public void startStocktaking(Long stocktakingId) {
        Long operatorId = UserContext.getUserId();
        stocktakingDomainService.startStocktaking(stocktakingId, operatorId);
    }

    /**
     * 录入实盘数量
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordCount(RecordCountDTO dto) {
        Long operatorId = UserContext.getUserId();
        stocktakingDomainService.recordCount(dto.getLineId(), dto.getActualQty(), operatorId);
    }

    /**
     * 提交盘点结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitStocktaking(Long stocktakingId) {
        Long operatorId = UserContext.getUserId();
        stocktakingDomainService.submitStocktaking(stocktakingId, operatorId);
    }

    /**
     * 审核差异并调整库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void reviewAndAdjust(Long stocktakingId) {
        Long operatorId = UserContext.getUserId();
        stocktakingDomainService.reviewAndAdjust(stocktakingId, operatorId);
    }

    /**
     * 查询盘点单详情（支持盲盘模式）
     */
    public StocktakingOrderVO getDetail(Long stocktakingId) {
        StocktakingOrder order = stocktakingOrderRepository.selectDetailById(stocktakingId);
        boolean isBlindMode = order != null && order.getCountMode() == CountMode.BLIND
                && order.getStatus() == StocktakingStatus.IN_PROGRESS;
        return toVO(order, isBlindMode);
    }

    /**
     * 分页查询
     */
    public IPage<StocktakingOrderVO> pageQuery(StocktakingQueryDTO dto) {
        Page<StocktakingOrder> page = new Page<>(dto.getCurrent(), dto.getSize());
        StocktakingStatus status = dto.getStatus() != null ? StocktakingStatus.valueOf(dto.getStatus()) : null;
        IPage<StocktakingOrder> result = stocktakingOrderRepository.selectPage(
                page, status, dto.getWarehouseId());
        return result.convert(o -> toVO(o, false));
    }

    // ==================== 私有方法 ====================

    private StocktakingOrderVO toVO(StocktakingOrder order, boolean isBlindMode) {
        if (order == null) return null;
        StocktakingOrderVO vo = new StocktakingOrderVO();
        vo.setId(order.getId());
        vo.setStocktakingNo(order.getStocktakingNo());
        vo.setStocktakingType(order.getStocktakingType() != null ? order.getStocktakingType().getCode() : null);
        vo.setStocktakingTypeLabel(order.getStocktakingType() != null ? order.getStocktakingType().getLabel() : null);
        vo.setCountMode(order.getCountMode() != null ? order.getCountMode().getCode() : null);
        vo.setCountModeLabel(order.getCountMode() != null ? order.getCountMode().getLabel() : null);
        vo.setWarehouseId(order.getWarehouseId());
        vo.setZoneCode(order.getZoneCode());
        vo.setStatus(order.getStatus() != null ? order.getStatus().getCode() : null);
        vo.setStatusLabel(order.getStatus() != null ? order.getStatus().getLabel() : null);
        vo.setPlannedDate(order.getPlannedDate() != null ? order.getPlannedDate().toString() : null);
        vo.setStartedAt(order.getStartedAt());
        vo.setCompletedAt(order.getCompletedAt());
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());

        if (order.getLines() != null) {
            List<StocktakingLine> lines = order.getLines();
            if (isBlindMode) {
                for (StocktakingLine line : lines) {
                    line.setSystemQty(null);
                }
            }
            vo.setLines(lines.stream().map(this::toLineVO).toList());
        } else {
            vo.setLines(List.of());
        }
        return vo;
    }

    private StocktakingLineVO toLineVO(StocktakingLine line) {
        StocktakingLineVO vo = new StocktakingLineVO();
        vo.setId(line.getId());
        vo.setInventoryType(line.getInventoryType() != null ? line.getInventoryType().getCode() : null);
        vo.setSkuId(line.getSkuId());
        vo.setMaterialId(line.getMaterialId());
        vo.setWarehouseId(line.getWarehouseId());
        vo.setLocationId(line.getLocationId());
        vo.setBatchNo(line.getBatchNo());
        vo.setSystemQty(line.getSystemQty());
        vo.setActualQty(line.getActualQty());
        vo.setDiffQty(line.getDiffQty());
        vo.setDiffStatus(line.getDiffStatus() != null ? line.getDiffStatus().getCode() : null);
        vo.setDiffReason(line.getDiffReason() != null ? line.getDiffReason().getCode() : null);
        vo.setNeedRecheck(line.getNeedRecheck());
        vo.setRemark(line.getRemark());
        return vo;
    }
}
