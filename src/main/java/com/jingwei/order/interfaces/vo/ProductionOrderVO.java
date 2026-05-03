package com.jingwei.order.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产订单 VO
 *
 * @author JingWei
 */
@Data
public class ProductionOrderVO {

    /** 订单ID */
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 计划生产日期 */
    private String planDate;

    /** 要求完工日期 */
    private String deadlineDate;

    /** 状态 */
    private String status;

    /** 状态中文标签 */
    private String statusLabel;

    /** 来源类型 */
    private String sourceType;

    /** 车间ID */
    private Long workshopId;

    /** 总数量 */
    private Integer totalQuantity;

    /** 已完工数量 */
    private Integer completedQuantity;

    /** 已入库数量 */
    private Integer stockedQuantity;

    /** 备注 */
    private String remark;

    /** 订单行列表 */
    private List<ProductionOrderLineVO> lines;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
