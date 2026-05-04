package com.jingwei.procurement.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MRP 计算结果 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class MrpResultVO {

    private Long id;
    private String batchNo;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String materialType;
    private BigDecimal grossDemand;
    private BigDecimal allocatedStock;
    private BigDecimal inTransitQuantity;
    private BigDecimal netDemand;
    private BigDecimal suggestedQuantity;
    private String unit;
    private Long suggestedSupplierId;
    private String suggestedSupplierName;
    private BigDecimal estimatedCost;
    private String status;
    private String statusLabel;
    private LocalDateTime snapshotTime;
    private String remark;
}
