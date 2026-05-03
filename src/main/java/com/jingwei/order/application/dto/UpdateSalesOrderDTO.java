package com.jingwei.order.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 编辑草稿订单 DTO
 * <p>
 * 仅 DRAFT 状态的订单允许编辑。
 * 编辑时采用全量替换策略：传入完整的订单行列表，后端先删旧行再插新行。
 * </p>
 *
 * @author JingWei
 */
@Data
public class UpdateSalesOrderDTO {

    /** 客户ID */
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    /** 季节ID */
    private Long seasonId;

    /** 要求交货日期 */
    private String deliveryDate;

    /** 业务员ID */
    private Long salesRepId;

    /** 备注 */
    @Size(max = 500, message = "备注不能超过500字")
    private String remark;

    /** 订单行列表（全量替换） */
    @NotNull(message = "订单行不能为空")
    @Size(min = 1, message = "订单至少需要一行明细")
    private List<SalesOrderLineCreateDTO> lines;
}
