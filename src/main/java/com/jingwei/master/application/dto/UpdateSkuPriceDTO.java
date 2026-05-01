package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 更新 SKU 价格请求 DTO
 * <p>
 * 支持按单个 SKU 更新或按 SPU 批量更新价格。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateSkuPriceDTO {

    /** SKU ID（必填） */
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    /** 成本价（可选） */
    private BigDecimal costPrice;

    /** 销售价（可选） */
    private BigDecimal salePrice;

    /** 批发价（可选） */
    private BigDecimal wholesalePrice;
}
