package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SKU 响应 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class SkuVO {

    private Long id;
    private String code;
    private String barcode;
    private Long spuId;
    private Long colorWayId;
    private Long sizeId;
    private BigDecimal costPrice;
    private BigDecimal salePrice;
    private BigDecimal wholesalePrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
