package com.jingwei.order.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.config.JsonbTypeHandler;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数量变更单实体
 * <p>
 * 对应数据库表 t_order_quantity_change。
 * 已确认的销售订单如需修改行数量，必须走变更单审批流程。
 * 变更单记录变更前后的尺码矩阵和差异矩阵，审批通过后才生效。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName(value = "t_order_quantity_change", autoResultMap = true)
public class OrderQuantityChange implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID，雪花算法生成 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 原订单ID */
    private Long orderId;

    /** 原订单行ID */
    private Long orderLineId;

    /** 变更前尺码矩阵（JSONB） */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Object sizeMatrixBefore;

    /** 变更后尺码矩阵（JSONB） */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Object sizeMatrixAfter;

    /** 差异矩阵（JSONB，after - before） */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Object diffMatrix;

    /** 变更原因 */
    private String reason;

    /** 状态：PENDING / APPROVED / REJECTED */
    private String status;

    /** 审批人ID */
    private Long approvedBy;

    /** 审批时间 */
    private LocalDateTime approvedAt;

    /** 创建人ID */
    private Long createdBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新人ID */
    private Long updatedBy;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
