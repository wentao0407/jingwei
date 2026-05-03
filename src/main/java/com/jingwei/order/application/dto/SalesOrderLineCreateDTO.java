package com.jingwei.order.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建销售订单行 DTO
 * <p>
 * 每行对应一个款式+颜色的组合，包含尺码矩阵数量。
 * </p>
 *
 * @author JingWei
 */
@Data
public class SalesOrderLineCreateDTO {

    /** 款式ID */
    @NotNull(message = "款式ID不能为空")
    private Long spuId;

    /** 颜色款ID */
    @NotNull(message = "颜色款ID不能为空")
    private Long colorWayId;

    /** 尺码组ID */
    @NotNull(message = "尺码组ID不能为空")
    private Long sizeGroupId;

    /** 尺码数量列表 */
    @NotNull(message = "尺码矩阵不能为空")
    private List<SizeEntryDTO> sizes;

    /** 单价（默认0） */
    private BigDecimal unitPrice;

    /** 折扣率（如0.95表示95折，默认1.0） */
    private BigDecimal discountRate;

    /** 本行交货日期（可覆盖主表） */
    private String deliveryDate;

    /** 行备注 */
    private String remark;
}
