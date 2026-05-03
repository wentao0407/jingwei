package com.jingwei.order.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 销售订单聚合根
 * <p>
 * 对应数据库表 t_order_sales，管理销售订单的全生命周期。
 * 订单编号由编码规则引擎自动生成（格式：SO-年月-5位流水号）。
 * </p>
 * <p>
 * 核心业务规则：
 * <ul>
 *   <li>只有 DRAFT 状态允许编辑订单内容（数量、价格等）</li>
 *   <li>PENDING_APPROVAL 状态等待审批，不可编辑</li>
 *   <li>REJECTED 状态可修改后重新提交，回到 PENDING_APPROVAL</li>
 *   <li>CONFIRMED 之后只能变更交期和备注，不能改数量（改数量走变更单流程）</li>
 *   <li>CANCELLED 是终态，不可恢复</li>
 *   <li>同一订单内不允许重复的款式+颜色组合</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_order_sales")
public class SalesOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 订单编号（编码规则生成，如 SO-202604-00001） */
    private String orderNo;

    /** 客户ID（外键→t_md_customer） */
    private Long customerId;

    /** 季节ID（外键→t_md_season） */
    private Long seasonId;

    /** 订单日期 */
    private LocalDate orderDate;

    /** 要求交货日期 */
    private LocalDate deliveryDate;

    /** 状态（DRAFT/PENDING_APPROVAL/REJECTED/CONFIRMED/PRODUCING/READY/SHIPPED/COMPLETED/CANCELLED） */
    private SalesOrderStatus status;

    /** 总数量（所有行矩阵求和，冗余字段） */
    private Integer totalQuantity;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 整单折扣金额 */
    private BigDecimal discountAmount;

    /** 实际金额 = total_amount - discount_amount */
    private BigDecimal actualAmount;

    /** 收款状态：UNPAID/PARTIAL/PAID */
    private String paymentStatus;

    /** 已收金额 */
    private BigDecimal paymentAmount;

    /** 业务员ID */
    private Long salesRepId;

    /** 备注 */
    private String remark;

    /** 订单行列表（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private List<SalesOrderLine> lines = new ArrayList<>();
}
