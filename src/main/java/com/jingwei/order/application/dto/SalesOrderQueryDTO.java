package com.jingwei.order.application.dto;

import lombok.Data;

/**
 * 销售订单分页查询 DTO
 * <p>
 * 支持按状态、客户、季节、订单编号、订单日期范围筛选。
 * </p>
 *
 * @author JingWei
 */
@Data
public class SalesOrderQueryDTO {

    /** 当前页码（从1开始） */
    private long current = 1;

    /** 每页条数 */
    private long size = 10;

    /** 状态筛选（可选） */
    private String status;

    /** 客户ID筛选（可选） */
    private Long customerId;

    /** 季节ID筛选（可选） */
    private Long seasonId;

    /** 订单编号搜索（模糊匹配，可选） */
    private String orderNo;

    /** 订单日期起始（可选，格式 yyyy-MM-dd） */
    private String orderDateStart;

    /** 订单日期结束（可选，格式 yyyy-MM-dd） */
    private String orderDateEnd;
}
