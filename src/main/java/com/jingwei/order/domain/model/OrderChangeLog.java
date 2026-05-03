package com.jingwei.order.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单变更日志实体
 * <p>
 * 对应数据库表 t_order_change_log，记录订单的所有变更历史。
 * 变更日志为追加型数据，不支持修改和删除，不继承 BaseEntity（无软删除/乐观锁）。
 * </p>
 * <p>
 * 变更类型：
 * <ul>
 *   <li>STATUS_CHANGE — 状态变更（如 DRAFT → PENDING_APPROVAL）</li>
 *   <li>FIELD_CHANGE — 字段变更（如交货日期修改）</li>
 *   <li>LINE_ADD — 新增订单行</li>
 *   <li>LINE_REMOVE — 删除订单行</li>
 *   <li>QUANTITY_CHANGE — 数量变更（走变更单流程）</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_order_change_log")
public class OrderChangeLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID，雪花算法生成 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 订单类型：SALES / PRODUCTION */
    private String orderType;

    /** 订单ID */
    private Long orderId;

    /** 订单行ID（NULL 表示主表变更） */
    private Long orderLineId;

    /** 变更类型：STATUS_CHANGE / FIELD_CHANGE / LINE_ADD / LINE_REMOVE / QUANTITY_CHANGE */
    private String changeType;

    /** 变更字段名（如 status, delivery_date, size_matrix） */
    private String fieldName;

    /** 变更前值 */
    private String oldValue;

    /** 变更后值 */
    private String newValue;

    /** 变更原因 */
    private String changeReason;

    /** 操作人ID */
    private Long operatedBy;

    /** 操作时间 */
    private LocalDateTime operatedAt;
}
