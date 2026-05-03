package com.jingwei.order.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingwei.master.domain.model.ColorWay;
import com.jingwei.master.domain.model.Customer;
import com.jingwei.master.domain.model.Season;
import com.jingwei.master.domain.model.Spu;
import com.jingwei.master.domain.repository.ColorWayRepository;
import com.jingwei.master.domain.repository.CustomerRepository;
import com.jingwei.master.domain.repository.SeasonRepository;
import com.jingwei.master.domain.repository.SpuRepository;
import com.jingwei.master.domain.service.CodingRuleDomainService;
import com.jingwei.order.application.dto.CreateSalesOrderDTO;
import com.jingwei.order.application.dto.SalesOrderLineCreateDTO;
import com.jingwei.order.application.dto.SalesOrderQueryDTO;
import com.jingwei.order.application.dto.UpdateSalesOrderDTO;
import com.jingwei.order.domain.model.SalesOrder;
import com.jingwei.order.domain.model.SalesOrderLine;

import com.jingwei.order.domain.model.SizeMatrix;
import com.jingwei.order.domain.service.SalesOrderDomainService;
import com.jingwei.order.interfaces.vo.SalesOrderLineVO;
import com.jingwei.order.interfaces.vo.SalesOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 销售订单应用服务
 * <p>
 * 负责销售订单 CRUD 的编排和事务边界管理。
 * 订单编号生成调用编码规则引擎，业务校验委托给 SalesOrderDomainService。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderApplicationService {

    private static final String SALES_ORDER_CODE_RULE = "SALES_ORDER";

    private final SalesOrderDomainService salesOrderDomainService;
    private final CodingRuleDomainService codingRuleDomainService;
    private final CustomerRepository customerRepository;
    private final SeasonRepository seasonRepository;
    private final SpuRepository spuRepository;
    private final ColorWayRepository colorWayRepository;

    /**
     * 创建销售订单
     * <p>
     * 编排流程：
     * <ol>
     *   <li>校验客户是否存在且为启用状态</li>
     *   <li>校验季节是否存在</li>
     *   <li>调用编码规则引擎生成订单编号</li>
     *   <li>组装 SalesOrder 和 SalesOrderLine 实体</li>
     *   <li>调用 DomainService 执行业务校验和持久化</li>
     * </ol>
     * </p>
     *
     * @param dto 创建请求
     * @return 销售订单 VO
     */
    @Transactional(rollbackFor = Exception.class)
    public SalesOrderVO createSalesOrder(CreateSalesOrderDTO dto) {
        // 1. 校验客户
        Customer customer = customerRepository.selectById(dto.getCustomerId());
        if (customer == null) {
            throw new com.jingwei.common.domain.model.BizException(
                    com.jingwei.common.domain.model.ErrorCode.DATA_NOT_FOUND, "客户不存在");
        }

        // 2. 校验季节（可选）
        Season season = null;
        if (dto.getSeasonId() != null) {
            season = seasonRepository.selectById(dto.getSeasonId());
        }

        // 3. 调用编码规则引擎生成订单编号
        String orderNo = codingRuleDomainService.generateCode(SALES_ORDER_CODE_RULE, java.util.Collections.emptyMap());

        // 4. 组装订单主表实体
        SalesOrder order = new SalesOrder();
        order.setOrderNo(orderNo);
        order.setCustomerId(dto.getCustomerId());
        order.setSeasonId(dto.getSeasonId());
        order.setOrderDate(dto.getOrderDate() != null ? LocalDate.parse(dto.getOrderDate()) : LocalDate.now());
        order.setDeliveryDate(dto.getDeliveryDate() != null ? LocalDate.parse(dto.getDeliveryDate()) : null);
        order.setSalesRepId(dto.getSalesRepId());
        order.setRemark(dto.getRemark() != null ? dto.getRemark() : "");
        order.setPaymentStatus("UNPAID");
        order.setPaymentAmount(BigDecimal.ZERO);

        // 5. 组装订单行实体
        List<SalesOrderLine> lines = buildOrderLines(dto.getLines());

        // 6. 调用 DomainService 执行业务校验和持久化
        SalesOrder saved = salesOrderDomainService.createOrder(order, lines);

        // 7. 转换为 VO（含客户名称、季节名称等冗余信息）
        return toSalesOrderVO(saved, customer, season);
    }

    /**
     * 编辑草稿订单
     * <p>
     * 仅 DRAFT 状态的订单允许编辑。采用全量替换策略。
     * </p>
     *
     * @param orderId 订单ID
     * @param dto     编辑请求
     * @return 销售订单 VO
     */
    @Transactional(rollbackFor = Exception.class)
    public SalesOrderVO updateSalesOrder(Long orderId, UpdateSalesOrderDTO dto) {
        // 校验客户
        Customer customer = customerRepository.selectById(dto.getCustomerId());
        if (customer == null) {
            throw new com.jingwei.common.domain.model.BizException(
                    com.jingwei.common.domain.model.ErrorCode.DATA_NOT_FOUND, "客户不存在");
        }

        Season season = null;
        if (dto.getSeasonId() != null) {
            season = seasonRepository.selectById(dto.getSeasonId());
        }

        // 组装更新实体
        SalesOrder order = new SalesOrder();
        order.setCustomerId(dto.getCustomerId());
        order.setSeasonId(dto.getSeasonId());
        order.setDeliveryDate(dto.getDeliveryDate() != null ? LocalDate.parse(dto.getDeliveryDate()) : null);
        order.setSalesRepId(dto.getSalesRepId());
        order.setRemark(dto.getRemark());

        // 组装订单行
        List<SalesOrderLine> lines = buildOrderLines(dto.getLines());

        SalesOrder updated = salesOrderDomainService.updateOrder(orderId, order, lines);
        return toSalesOrderVO(updated, customer, season);
    }

    /**
     * 删除草稿订单
     *
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSalesOrder(Long orderId) {
        salesOrderDomainService.deleteOrder(orderId);
    }

    /**
     * 查询订单详情（含矩阵展开）
     *
     * @param orderId 订单ID
     * @return 销售订单 VO
     */
    public SalesOrderVO getSalesOrderDetail(Long orderId) {
        SalesOrder order = salesOrderDomainService.getOrderDetail(orderId);
        return toSalesOrderVO(order);
    }

    /**
     * 分页查询销售订单
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    public IPage<SalesOrderVO> pageQuery(SalesOrderQueryDTO dto) {
        Page<SalesOrder> page = new Page<>(dto.getCurrent(), dto.getSize());

        IPage<SalesOrder> orderPage = salesOrderDomainService.getSalesOrderRepository()
                .selectPage(page, dto.getStatus(), dto.getCustomerId(), dto.getSeasonId(),
                        dto.getOrderNo(), dto.getOrderDateStart(), dto.getOrderDateEnd());

        return orderPage.convert(this::toSalesOrderVO);
    }

    // ==================== 私有方法 ====================

    /**
     * 将 DTO 列表转换为 SalesOrderLine 实体列表
     *
     * @param lineDTOs 订单行 DTO 列表
     * @return 订单行实体列表
     */
    private List<SalesOrderLine> buildOrderLines(List<SalesOrderLineCreateDTO> lineDTOs) {
        List<SalesOrderLine> lines = new ArrayList<>();
        for (SalesOrderLineCreateDTO lineDTO : lineDTOs) {
            SalesOrderLine line = new SalesOrderLine();
            line.setSpuId(lineDTO.getSpuId());
            line.setColorWayId(lineDTO.getColorWayId());

            // 构建 SizeMatrix 值对象
            List<SizeMatrix.SizeEntry> sizes = lineDTO.getSizes().stream()
                    .map(s -> new SizeMatrix.SizeEntry(s.getSizeId(), s.getCode(), s.getQuantity()))
                    .toList();
            SizeMatrix sizeMatrix = new SizeMatrix(lineDTO.getSizeGroupId(), sizes);
            line.setSizeMatrix(sizeMatrix);

            line.setUnitPrice(lineDTO.getUnitPrice() != null ? lineDTO.getUnitPrice() : BigDecimal.ZERO);
            line.setDiscountRate(lineDTO.getDiscountRate() != null ? lineDTO.getDiscountRate() : BigDecimal.ONE);
            line.setDeliveryDate(lineDTO.getDeliveryDate() != null ? LocalDate.parse(lineDTO.getDeliveryDate()) : null);
            line.setRemark(lineDTO.getRemark());

            lines.add(line);
        }
        return lines;
    }

    /**
     * 将 SalesOrder 实体转换为 SalesOrderVO
     * <p>
     * 自动补充客户名称、季节名称等冗余展示信息。
     * </p>
     *
     * @param order 订单实体
     * @return 销售订单 VO
     */
    private SalesOrderVO toSalesOrderVO(SalesOrder order) {
        Customer customer = null;
        if (order.getCustomerId() != null) {
            customer = customerRepository.selectById(order.getCustomerId());
        }
        Season season = null;
        if (order.getSeasonId() != null) {
            season = seasonRepository.selectById(order.getSeasonId());
        }
        return toSalesOrderVO(order, customer, season);
    }

    /**
     * 将 SalesOrder 实体转换为 SalesOrderVO（含客户和季节信息）
     *
     * @param order    订单实体
     * @param customer 客户实体（可为 null）
     * @param season   季节实体（可为 null）
     * @return 销售订单 VO
     */
    private SalesOrderVO toSalesOrderVO(SalesOrder order, Customer customer, Season season) {
        SalesOrderVO vo = new SalesOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setCustomerId(order.getCustomerId());
        vo.setCustomerName(customer != null ? customer.getName() : null);
        vo.setCustomerLevel(customer != null && customer.getLevel() != null
                ? customer.getLevel().name() : null);
        vo.setSeasonId(order.getSeasonId());
        vo.setSeasonName(season != null ? season.getName() : null);
        vo.setOrderDate(order.getOrderDate() != null ? order.getOrderDate().toString() : null);
        vo.setDeliveryDate(order.getDeliveryDate() != null ? order.getDeliveryDate().toString() : null);
        vo.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        vo.setStatusLabel(order.getStatus() != null ? order.getStatus().getLabel() : null);
        vo.setTotalQuantity(order.getTotalQuantity());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setActualAmount(order.getActualAmount());
        vo.setPaymentStatus(order.getPaymentStatus());
        vo.setPaymentAmount(order.getPaymentAmount());
        vo.setSalesRepId(order.getSalesRepId());
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());

        // 转换订单行
        if (order.getLines() != null && !order.getLines().isEmpty()) {
            vo.setLines(order.getLines().stream()
                    .map(this::toSalesOrderLineVO)
                    .toList());
        } else {
            vo.setLines(List.of());
        }

        return vo;
    }

    /**
     * 将 SalesOrderLine 实体转换为 SalesOrderLineVO
     *
     * @param line 订单行实体
     * @return 订单行 VO
     */
    private SalesOrderLineVO toSalesOrderLineVO(SalesOrderLine line) {
        SalesOrderLineVO vo = new SalesOrderLineVO();
        vo.setId(line.getId());
        vo.setLineNo(line.getLineNo());
        vo.setSpuId(line.getSpuId());
        vo.setColorWayId(line.getColorWayId());

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

        // 尺码矩阵转换为 Map（前端可直接使用 JSON 格式）
        if (line.getSizeMatrix() != null) {
            SizeMatrix matrix = line.getSizeMatrix();
            vo.setSizeMatrix(Map.of(
                    "sizeGroupId", matrix.getSizeGroupId(),
                    "sizes", matrix.getSizes(),
                    "totalQuantity", matrix.getTotalQuantity()
            ));
        }

        vo.setTotalQuantity(line.getTotalQuantity());
        vo.setUnitPrice(line.getUnitPrice());
        vo.setLineAmount(line.getLineAmount());
        vo.setDiscountRate(line.getDiscountRate());
        vo.setDiscountAmount(line.getDiscountAmount());
        vo.setActualAmount(line.getActualAmount());
        vo.setDeliveryDate(line.getDeliveryDate() != null ? line.getDeliveryDate().toString() : null);
        vo.setRemark(line.getRemark());

        return vo;
    }
}
