package com.jingwei.inventory.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 盘点行 VO
 *
 * @author JingWei
 */
@Data
public class StocktakingLineVO {
    private Long id;
    private String inventoryType;
    private Long skuId;
    private String skuCode;
    private Long materialId;
    private String materialName;
    private Long warehouseId;
    private Long locationId;
    private String locationCode;
    private String batchNo;
    /** 系统数量（盲盘模式下为空） */
    private BigDecimal systemQty;
    private BigDecimal actualQty;
    private BigDecimal diffQty;
    private String diffStatus;
    private String diffReason;
    private Boolean needRecheck;
    private String remark;
}
