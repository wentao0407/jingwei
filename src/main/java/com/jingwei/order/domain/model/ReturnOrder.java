package com.jingwei.order.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 退货单聚合根
 * <p>
 * 对应数据库表 t_order_return，管理退货单的全生命周期。
 * 退货单号由编码规则引擎自动生成（格式：RT-年月日-4位流水号）。
 * </p>
 * <p>
 * 核心业务规则：
 * <ul>
 *   <li>只有 DRAFT 状态允许编辑退货内容</li>
 *   <li>退货数量不能超过原订单对应 SKU 的已发货数量（累计校验）</li>
 *   <li>退货申请需经过审批引擎审批</li>
 *   <li>审批通过后仓库才能收货</li>
 *   <li>收货完成后进入质检环节</li>
 *   <li>质检合格品入库可用库存，不合格品标记报废</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_order_return")
public class ReturnOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 退货单号（编码规则生成，如 RT-20260505-0001） */
    private String returnNo;

    /** 退货类型 */
    private ReturnType returnType;

    /** 原销售订单ID */
    private Long salesOrderId;

    /** 原销售订单编号（冗余） */
    private String salesOrderNo;

    /** 客户ID */
    private Long customerId;

    /** 退货原因 */
    private String reason;

    /** 状态 */
    private ReturnStatus status;

    /** 退货总数量（所有行求和，冗余字段） */
    private Integer totalQuantity;

    /** 关联的退货入库单ID */
    private Long inboundOrderId;

    /** 审批人 */
    private Long approvedBy;

    /** 审批时间 */
    private LocalDateTime approvedAt;

    /** 备注 */
    private String remark;

    /** 退货行列表（非数据库字段） */
    @TableField(exist = false)
    private List<ReturnOrderLine> lines = new ArrayList<>();
}
