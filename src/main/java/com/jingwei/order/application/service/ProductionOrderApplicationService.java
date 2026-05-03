package com.jingwei.order.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.common.domain.model.BizException;
import com.jingwei.common.domain.model.ErrorCode;
import com.jingwei.common.domain.model.UserContext;
import com.jingwei.master.domain.model.ColorWay;
import com.jingwei.master.domain.model.Spu;
import com.jingwei.master.domain.repository.ColorWayRepository;
import com.jingwei.master.domain.repository.SpuRepository;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.order.application.dto.CreateProductionOrderDTO;
import com.jingwei.order.application.dto.ProductionOrderLineCreateDTO;
import com.jingwei.order.application.dto.ProductionOrderQueryDTO;
import com.jingwei.order.application.dto.UpdateProductionOrderDTO;
import com.jingwei.order.domain.model.ProductionOrder;
import com.jingwei.order.domain.model.ProductionOrderLine;
import com.jingwei.order.domain.model.ProductionOrderStatus;
import com.jingwei.order.domain.model.SizeMatrix;
import com.jingwei.order.domain.service.ProductionOrderDomainService;
import com.jingwei.order.interfaces.vo.ProductionOrderLineVO;
import com.jingwei.order.interfaces.vo.ProductionOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 生产订单应用服务
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionOrderApplicationService {

    private static final String PRODUCTION_ORDER_CODE_RULE = "PRODUCTION_ORDER";

    private final ProductionOrderDomainService productionOrderDomainService;
    private final CodingRuleDomainService codingRuleDomainService;
    private final SpuRepository spuRepository;
    private final ColorWayRepository colorWayRepository;

    /**
     * 创建生产订单
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductionOrderVO createProductionOrder(CreateProductionOrderDTO dto) {
        // 生成订单编号
        String orderNo = codingRuleDomainService.generateCode(
                PRODUCTION_ORDER_CODE_RULE, java.util.Collections.emptyMap());

        // 组装主表
        ProductionOrder order = new ProductionOrder();
        order.setOrderNo(orderNo);
        order.setSourceType(dto.getSourceType());
        order.setWorkshopId(dto.getWorkshopId());
        order.setPlanDate(dto.getPlanDate() != null ? LocalDate.parse(dto.getPlanDate()) : null);
        order.setDeadlineDate(dto.getDeadlineDate() != null ? LocalDate.parse(dto.getDeadlineDate()) : null);
        order.setRemark(dto.getRemark() != null ? dto.getRemark() : "");

        // 组装行
        List<ProductionOrderLine> lines = buildOrderLines(dto.getLines());

        ProductionOrder saved = productionOrderDomainService.createOrder(order, lines);
        return toProductionOrderVO(saved);
    }

    /**
     * 编辑生产订单
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductionOrderVO updateProductionOrder(Long orderId, UpdateProductionOrderDTO dto) {
        Long operatorId = UserContext.getUserId();

        ProductionOrder order = new ProductionOrder();
        order.setPlanDate(dto.getPlanDate() != null ? LocalDate.parse(dto.getPlanDate()) : null);
        order.setDeadlineDate(dto.getDeadlineDate() != null ? LocalDate.parse(dto.getDeadlineDate()) : null);
        order.setWorkshopId(dto.getWorkshopId());
        order.setRemark(dto.getRemark());

        List<ProductionOrderLine> lines = buildOrderLines(dto.getLines());

        ProductionOrder updated = productionOrderDomainService.updateOrder(orderId, order, lines, operatorId);
        return toProductionOrderVO(updated);
    }

    /**
     * 删除生产订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteProductionOrder(Long orderId) {
        productionOrderDomainService.deleteOrder(orderId);
    }

    /**
     * 查询订单详情
     */
    public ProductionOrderVO getDetail(Long orderId) {
        ProductionOrder order = productionOrderDomainService.getOrderDetail(orderId);
        return toProductionOrderVO(order);
    }

    /**
     * 分页查询
     */
    public IPage<ProductionOrderVO> pageQuery(ProductionOrderQueryDTO dto) {
        Page<ProductionOrder> page = new Page<>(dto.getCurrent(), dto.getSize());

        IPage<ProductionOrder> orderPage = productionOrderDomainService.getProductionOrderRepository()
                .selectPage(page, dto.getStatus(), dto.getOrderNo(),
                        dto.getPlanDateStart(), dto.getPlanDateEnd());

        return orderPage.convert(this::toProductionOrderVO);
    }

    // ==================== 私有方法 ====================

    private List<ProductionOrderLine> buildOrderLines(List<ProductionOrderLineCreateDTO> lineDTOs) {
        List<ProductionOrderLine> lines = new ArrayList<>();
        for (ProductionOrderLineCreateDTO lineDTO : lineDTOs) {
            ProductionOrderLine line = new ProductionOrderLine();
            line.setSpuId(lineDTO.getSpuId());
            line.setColorWayId(lineDTO.getColorWayId());
            line.setBomId(lineDTO.getBomId());
            line.setSkipCutting(lineDTO.getSkipCutting() != null ? lineDTO.getSkipCutting() : false);

            // 构建 SizeMatrix
            List<SizeMatrix.SizeEntry> sizes = lineDTO.getSizes().stream()
                    .map(s -> new SizeMatrix.SizeEntry(s.getSizeId(), s.getCode(), s.getQuantity()))
                    .toList();
            line.setSizeMatrix(new SizeMatrix(lineDTO.getSizeGroupId(), sizes));

            line.setRemark(lineDTO.getRemark());
            lines.add(line);
        }
        return lines;
    }

    private ProductionOrderVO toProductionOrderVO(ProductionOrder order) {
        ProductionOrderVO vo = new ProductionOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPlanDate(order.getPlanDate() != null ? order.getPlanDate().toString() : null);
        vo.setDeadlineDate(order.getDeadlineDate() != null ? order.getDeadlineDate().toString() : null);
        vo.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        vo.setStatusLabel(order.getStatus() != null ? order.getStatus().getLabel() : null);
        vo.setSourceType(order.getSourceType());
        vo.setWorkshopId(order.getWorkshopId());
        vo.setTotalQuantity(order.getTotalQuantity());
        vo.setCompletedQuantity(order.getCompletedQuantity());
        vo.setStockedQuantity(order.getStockedQuantity());
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());

        if (order.getLines() != null && !order.getLines().isEmpty()) {
            vo.setLines(order.getLines().stream()
                    .map(this::toProductionOrderLineVO)
                    .toList());
        } else {
            vo.setLines(List.of());
        }

        return vo;
    }

    private ProductionOrderLineVO toProductionOrderLineVO(ProductionOrderLine line) {
        ProductionOrderLineVO vo = new ProductionOrderLineVO();
        vo.setId(line.getId());
        vo.setLineNo(line.getLineNo());
        vo.setSpuId(line.getSpuId());
        vo.setColorWayId(line.getColorWayId());
        vo.setBomId(line.getBomId());

        // 补充款式和颜色展示信息
        if (line.getSpuId() != null) {
            Spu spu = spuRepository.selectById(line.getSpuId());
            if (spu != null) {
                vo.setSpuCode(spu.getCode());
                vo.setSpuName(spu.getName());
            }
        }
        if (line.getColorWayId() != null) {
            ColorWay colorWay = colorWayRepository.selectById(line.getColorWayId());
            if (colorWay != null) {
                vo.setColorName(colorWay.getColorName());
                vo.setColorCode(colorWay.getColorCode());
            }
        }

        // 尺码矩阵转换
        if (line.getSizeMatrix() != null) {
            SizeMatrix matrix = line.getSizeMatrix();
            vo.setSizeMatrix(Map.of(
                    "sizeGroupId", matrix.getSizeGroupId(),
                    "sizes", matrix.getSizes(),
                    "totalQuantity", matrix.getTotalQuantity()
            ));
        }

        vo.setTotalQuantity(line.getTotalQuantity());
        vo.setCompletedQuantity(line.getCompletedQuantity());
        vo.setStockedQuantity(line.getStockedQuantity());
        vo.setSkipCutting(line.getSkipCutting());
        vo.setStatus(line.getStatus() != null ? line.getStatus().name() : null);
        vo.setStatusLabel(line.getStatus() != null ? line.getStatus().getLabel() : null);
        vo.setRemark(line.getRemark());

        return vo;
    }
}
