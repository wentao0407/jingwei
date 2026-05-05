package com.jingwei.order.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jingwei.common.config.RequirePermission;
import com.jingwei.common.domain.model.R;
import com.jingwei.order.application.dto.ConvertToProductionDTO;
import com.jingwei.order.application.dto.CreateSalesOrderDTO;
import com.jingwei.order.application.dto.QuantityChangeCreateDTO;
import com.jingwei.order.application.dto.SalesOrderQueryDTO;
import com.jingwei.order.application.dto.UpdateSalesOrderDTO;
import com.jingwei.order.application.service.OrderConvertApplicationService;
import com.jingwei.order.application.service.SalesOrderApplicationService;
import com.jingwei.order.interfaces.vo.ConvertResultVO;
import com.jingwei.order.interfaces.vo.OrderTimelineVO;
import com.jingwei.order.interfaces.vo.QuantityChangeVO;
import com.jingwei.order.interfaces.vo.SalesOrderVO;
import jakarta.validation.Valid;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 销售订单 Controller
 * <p>
 * 提供销售订单的 CRUD 接口。
 * 所有接口统一使用 POST 方法。
 * </p>
 *
 * @author JingWei
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderApplicationService salesOrderApplicationService;
    private final OrderConvertApplicationService orderConvertApplicationService;

    /**
     * 创建销售订单
     * <p>
     * 订单编号由编码规则引擎自动生成（格式：SO-年月-5位流水号），
     * 状态默认 DRAFT，收款状态默认 UNPAID。
     * </p>
     */
    @RequirePermission("order:sales:create")
    @PostMapping("/order/sales/create")
    public R<SalesOrderVO> createSalesOrder(@Valid @RequestBody CreateSalesOrderDTO dto) {
        return R.ok(salesOrderApplicationService.createSalesOrder(dto));
    }

    /**
     * 编辑草稿订单
     * <p>
     * 仅 DRAFT 状态的订单允许编辑，采用全量替换策略。
     * </p>
     */
    @RequirePermission("order:sales:update")
    @PostMapping("/order/sales/update")
    public R<SalesOrderVO> updateSalesOrder(@RequestParam Long orderId,
                                             @Valid @RequestBody UpdateSalesOrderDTO dto) {
        return R.ok(salesOrderApplicationService.updateSalesOrder(orderId, dto));
    }

    /**
     * 删除草稿订单
     * <p>
     * 仅 DRAFT 状态的订单允许删除。
     * </p>
     */
    @RequirePermission("order:sales:delete")
    @PostMapping("/order/sales/delete")
    public R<Void> deleteSalesOrder(@RequestParam Long orderId) {
        salesOrderApplicationService.deleteSalesOrder(orderId);
        return R.ok();
    }

    /**
     * 提交订单审批
     * <p>
     * 仅 DRAFT 状态可提交。提交后触发审批引擎，
     * 若无审批配置则自动通过，状态直接变为 CONFIRMED。
     * </p>
     */
    @RequirePermission("order:sales:submit")
    @PostMapping("/order/sales/submit")
    public R<Void> submitOrder(@RequestParam Long orderId) {
        salesOrderApplicationService.submitOrder(orderId);
        return R.ok();
    }

    /**
     * 修改后重新提交
     * <p>
     * 仅 REJECTED 状态可重新提交。提交后触发审批引擎。
     * </p>
     */
    @RequirePermission("order:sales:resubmit")
    @PostMapping("/order/sales/resubmit")
    public R<Void> resubmitOrder(@RequestParam Long orderId) {
        salesOrderApplicationService.resubmitOrder(orderId);
        return R.ok();
    }

    /**
     * 取消订单
     * <p>
     * DRAFT 或 CONFIRMED 状态可取消。
     * CONFIRMED 状态取消需未关联生产订单。
     * </p>
     */
    @RequirePermission("order:sales:cancel")
    @PostMapping("/order/sales/cancel")
    public R<Void> cancelOrder(@RequestParam Long orderId) {
        salesOrderApplicationService.cancelOrder(orderId);
        return R.ok();
    }

    /**
     * 查询订单详情（含矩阵展开）
     * <p>
     * 返回订单主表信息、订单行列表及尺码矩阵数据，
     * 同时补充客户名称、季节名称、款式名称等冗余展示信息。
     * </p>
     */
    @PostMapping("/order/sales/detail")
    public R<SalesOrderVO> getSalesOrderDetail(@RequestParam Long orderId) {
        return R.ok(salesOrderApplicationService.getSalesOrderDetail(orderId));
    }

    /**
     * 分页查询销售订单
     * <p>
     * 支持按状态、客户、季节、订单编号、订单日期范围筛选。
     * </p>
     */
    @PostMapping("/order/sales/page")
    public R<IPage<SalesOrderVO>> pageQuery(@Valid @RequestBody SalesOrderQueryDTO dto) {
        return R.ok(salesOrderApplicationService.pageQuery(dto));
    }

    /**
     * 查询订单变更时间线
     * <p>
     * 按时间倒序返回订单的完整变更历史，包含状态变更、字段变更、数量变更等。
     * </p>
     */
    @RequirePermission("order:sales:timeline")
    @PostMapping("/order/sales/timeline")
    public R<List<OrderTimelineVO>> getTimeline(@RequestParam Long orderId) {
        return R.ok(salesOrderApplicationService.getTimeline(orderId));
    }

    /**
     * 创建数量变更单
     * <p>
     * 已确认的订单修改数量需走变更单审批流程。
     * 系统自动计算差异矩阵，提交后等待审批。
     * </p>
     */
    @RequirePermission("order:sales:quantity-change")
    @PostMapping("/order/sales/quantity-change")
    public R<QuantityChangeVO> createQuantityChange(@Valid @RequestBody QuantityChangeCreateDTO dto) {
        return R.ok(salesOrderApplicationService.createQuantityChange(dto));
    }

    /**
     * 查询订单的数量变更单列表
     * <p>
     * 返回指定订单的所有数量变更单，含差异矩阵。
     * </p>
     */
    @PostMapping("/order/sales/quantity-change/list")
    public R<List<QuantityChangeVO>> listQuantityChanges(@RequestParam Long orderId) {
        return R.ok(salesOrderApplicationService.listQuantityChanges(orderId));
    }

    // ==================== 订单转化接口（T-24） ====================

    /**
     * 从销售订单生成生产订单
     * <p>
     * 仅 CONFIRMED 状态的销售订单允许转化。
     * 用户选择若干行后，系统按款式分组合并，自动关联 BOM，生成生产订单。
     * 转化后销售订单状态自动变为 PRODUCING。
     * </p>
     * <p>
     * 业务规则：
     * <ul>
     *   <li>只有 CONFIRMED 状态的订单允许转化</li>
     *   <li>选中的行必须属于该销售订单</li>
     *   <li>同一行不可重复全额转化</li>
     *   <li>同款不同颜色的行合并为一张生产订单</li>
     *   <li>自动关联 SPU 的已审批 BOM</li>
     *   <li>转化后建立 order_production_source 多对多关联</li>
     * </ul>
     * </p>
     */
    @RequirePermission("order:sales:convert")
    @PostMapping("/order/sales/convert-to-production")
    public R<ConvertResultVO> convertToProduction(@Valid @RequestBody ConvertToProductionDTO dto) {
        return R.ok(orderConvertApplicationService.convertToProduction(dto));
    }
}
