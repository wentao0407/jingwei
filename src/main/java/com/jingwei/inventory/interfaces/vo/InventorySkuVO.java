package com.jingwei.inventory.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 成品库存 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class InventorySkuVO {
    private Long id;
    private Long skuId;
    private Long warehouseId;
    private Long locationId;
    private String batchNo;
    private Integer availableQty;
    private Integer lockedQty;
    private Integer qcQty;
    private Integer totalQty;
    private Integer inTransitQty;
    private BigDecimal unitCost;
    private LocalDate lastInboundDate;
    private LocalDate lastOutboundDate;
}
