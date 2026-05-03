package com.jingwei.order.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.config.JsonbTypeHandler;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 销售订单行实体 — 每行一个颜色款
 * <p>
 * 对应数据库表 t_order_sales_line。
 * 采用方案B存储模型：每个颜色款(Color-way)一行，数量存为尺码矩阵 JSONB。
 * </p>
 * <p>
 * 服装行业用户的思维方式是"这个款黑色要多少"，不是"黑色S码要多少"，
 * 因此矩阵存储比逐SKU建行更贴合业务交互。
 * </p>
 * <p>
 * 金额计算规则：
 * <ul>
 *   <li>行金额 = total_quantity × unit_price</li>
 *   <li>行折扣金额 = 行金额 × (1 - discount_rate)</li>
 *   <li>行实际金额 = 行金额 - 行折扣金额 = 行金额 × discount_rate</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName(value = "t_order_sales_line", autoResultMap = true)
public class SalesOrderLine extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 订单ID（外键→t_order_sales） */
    private Long orderId;

    /** 行号（1,2,3...） */
    private Integer lineNo;

    /** 款式ID（外键→t_md_spu） */
    private Long spuId;

    /** 颜色款ID（外键→t_md_color_way） */
    private Long colorWayId;

    /** 尺码矩阵数量（JSONB，核心字段） */
    @TableField(typeHandler = SizeMatrixTypeHandler.class)
    private SizeMatrix sizeMatrix;

    /** 本行总数量（矩阵求和，冗余） */
    private Integer totalQuantity;

    /** 单价（本行统一单价） */
    private BigDecimal unitPrice;

    /** 行金额 = total_quantity × unit_price */
    private BigDecimal lineAmount;

    /** 行折扣率（如0.95表示95折） */
    private BigDecimal discountRate;

    /** 行折扣金额 */
    private BigDecimal discountAmount;

    /** 行实际金额 */
    private BigDecimal actualAmount;

    /** 本行交货日期（可覆盖主表） */
    private LocalDate deliveryDate;

    /** 行备注 */
    private String remark;
}
