package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 批量更新 SKU 价格请求 DTO
 * <p>
 * 支持两种批量模式：
 * <ul>
 *   <li>按 SPU 维度：传入 spuId，更新该款式下所有 SKU 的价格</li>
 *   <li>按颜色款维度：传入 spuId + colorWayId，仅更新该颜色下所有 SKU 的价格</li>
 * </ul>
 * 传入的价格字段为可选，只更新非空字段。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class BatchUpdateSkuPriceDTO {

    /** 款式ID（必填） */
    @NotNull(message = "款式ID不能为空")
    private Long spuId;

    /** 颜色款ID（可选，不传则按 SPU 维度批量更新） */
    private Long colorWayId;

    /** 成本价（可选，传入则批量设置） */
    private BigDecimal costPrice;

    /** 销售价（可选，传入则批量设置） */
    private BigDecimal salePrice;

    /** 批发价（可选，传入则批量设置） */
    private BigDecimal wholesalePrice;
}
