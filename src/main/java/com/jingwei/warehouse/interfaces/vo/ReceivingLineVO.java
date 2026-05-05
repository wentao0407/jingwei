package com.jingwei.warehouse.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收货行 VO
 *
 * @author JingWei
 */
@Data
public class ReceivingLineVO {
    private Long id;
    private Long asnLineId;
    private Long materialId;
    private String materialName;
    private BigDecimal expectedQty;
    private BigDecimal receivedQty;
    private Integer rollCount;
    private BigDecimal differenceQty;
    private String differenceReason;
    private String batchNo;
    private String qcStatus;
    private String qcStatusLabel;
    private String putawayStatus;
    private String putawayStatusLabel;
    private Long putawayLocationId;
    private String putawayLocationCode;
    private String remark;
}
