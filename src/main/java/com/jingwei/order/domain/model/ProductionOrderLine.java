package com.jingwei.order.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 生产订单行实体 — 每行一个颜色款
 * <p>
 * 对应数据库表 t_order_production_line。
 * 每行有独立的状态，允许同一订单中不同颜色处于不同生产阶段。
 * </p>
 * <p>
 * 与销售订单行的区别：
 * <ul>
 *   <li>多了 bom_id（BOM 用料依据，下达时必须有）</li>
 *   <li>多了 skip_cutting（针织类跳过裁剪）</li>
 *   <li>多了独立 status（行状态）</li>
 *   <li>多了 completed_quantity / stocked_quantity（生产进度跟踪）</li>
 *   <li>没有金额字段（生产订单不涉及定价）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName(value = "t_order_production_line", autoResultMap = true)
public class ProductionOrderLine extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 生产订单ID（外键→t_order_production） */
    private Long orderId;

    /** 行号（1,2,3...） */
    private Integer lineNo;

    /** 款式ID（外键→t_md_spu） */
    private Long spuId;

    /** 颜色款ID（外键→t_md_color_way） */
    private Long colorWayId;

    /** BOM ID（外键→t_bom，下达时必须有） */
    private Long bomId;

    /** 尺码矩阵数量（JSONB，结构同销售订单） */
    @TableField(typeHandler = SizeMatrixTypeHandler.class)
    private SizeMatrix sizeMatrix;

    /** 本行总数量（矩阵求和，冗余） */
    private Integer totalQuantity;

    /** 本行已完工数量 */
    private Integer completedQuantity;

    /** 本行已入库数量 */
    private Integer stockedQuantity;

    /** 是否跳过裁剪环节（针织类默认TRUE） */
    private Boolean skipCutting;

    /** 行状态（允许不同行处于不同生产阶段） */
    private ProductionOrderStatus status;

    /** 行备注 */
    private String remark;
}
