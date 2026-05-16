package com.jingwei.inventory.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 原料库存 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class InventoryMaterialVO {
    private Long id;
    private Long materialId;
    private Long warehouseId;
    private Long locationId;
    private String batchNo;
    private Long supplierId;
    private Long procurementOrderId;
    private BigDecimal availableQty;
    private BigDecimal lockedQty;
    private BigDecimal qcQty;
    private BigDecimal totalQty;
    private BigDecimal inTransitQty;
    private BigDecimal unitCost;
    private Integer rollCount;
    private LocalDate lastInboundDate;
    private LocalDate lastOutboundDate;
}
