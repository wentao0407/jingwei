package com.jingwei.order.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建销售订单 DTO
 * <p>
 * 包含订单主表信息和订单行列表。
 * 订单编号由编码规则引擎自动生成，不需要前端传入。
 * </p>
 *
 * @author JingWei
 */
@Data
public class CreateSalesOrderDTO {

    /** 客户ID */
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    /** 季节ID */
    private Long seasonId;

    /** 订单日期 */
    @NotNull(message = "订单日期不能为空")
    private String orderDate;

    /** 要求交货日期 */
    private String deliveryDate;

    /** 业务员ID */
    private Long salesRepId;

    /** 备注 */
    @Size(max = 500, message = "备注不能超过500字")
    private String remark;

    /** 订单行列表 */
    @NotNull(message = "订单行不能为空")
    @Size(min = 1, message = "订单至少需要一行明细")
    private List<SalesOrderLineCreateDTO> lines;
}
