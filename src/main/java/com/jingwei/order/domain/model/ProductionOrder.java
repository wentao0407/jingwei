package com.jingwei.order.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 生产订单聚合根
 * <p>
 * 对应数据库表 t_order_production，管理生产订单的全生命周期。
 * 生产订单可以独立创建，也可以从销售订单转化（多对多关联）。
 * </p>
 * <p>
 * 核心业务规则：
 * <ul>
 *   <li>每行有独立状态，主表状态取所有行的最滞后状态</li>
 *   <li>只有 DRAFT 状态允许编辑订单内容</li>
 *   <li>下达时必须有 BOM 和数量</li>
 *   <li>skip_cutting 标记允许针织类跳过裁剪环节</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_order_production")
public class ProductionOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 生产订单编号（编码规则生成，如 MO-202605-00001） */
    private String orderNo;

    /** 计划生产日期 */
    private LocalDate planDate;

    /** 要求完工日期 */
    private LocalDate deadlineDate;

    /** 状态（DRAFT/RELEASED/PLANNED/CUTTING/SEWING/FINISHING/COMPLETED/STOCKED） */
    private ProductionOrderStatus status;

    /** 来源类型：MANUAL=独立创建，SALES_ORDER=从销售订单转化 */
    private String sourceType;

    /** 车间ID（可选） */
    private Long workshopId;

    /** 总数量（所有行求和，冗余字段） */
    private Integer totalQuantity;

    /** 已完工数量 */
    private Integer completedQuantity;

    /** 已入库数量 */
    private Integer stockedQuantity;

    /** 备注 */
    private String remark;

    /** 订单行列表（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private List<ProductionOrderLine> lines = new ArrayList<>();
}
