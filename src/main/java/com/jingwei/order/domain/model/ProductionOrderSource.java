package com.jingwei.order.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 生产订单与销售订单关联实体（多对多）
 * <p>
 * 对应数据库表 t_order_production_source。
 * 记录生产订单的每一行来自哪个销售订单的哪一行，以及分配的数量。
 * </p>
 * <p>
 * 关联关系：
 * <ul>
 *   <li>一张销售订单可拆分为多张生产订单</li>
 *   <li>多张销售订单可合并为一张生产订单</li>
 *   <li>每条记录表示"生产订单的某行从销售订单的某行分配了多少数量"</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_order_production_source")
public class ProductionOrderSource implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID，雪花算法生成 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 生产订单ID */
    private Long productionOrderId;

    /** 生产订单行ID */
    private Long productionLineId;

    /** 销售订单ID */
    private Long salesOrderId;

    /** 销售订单行ID */
    private Long salesLineId;

    /** 从该销售订单分配的数量 */
    private Integer allocatedQuantity;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
