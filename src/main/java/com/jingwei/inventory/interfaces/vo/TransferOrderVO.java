package com.jingwei.inventory.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 调拨单 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class TransferOrderVO {

    private Long id;
    private String transferNo;
    private Long sourceWarehouseId;
    private String sourceWarehouseName;
    private Long targetWarehouseId;
    private String targetWarehouseName;
    private String status;
    private String statusLabel;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TransferOrderLineVO> lines;

    @Getter
    @Setter
    public static class TransferOrderLineVO {
        private Long id;
        private String inventoryType;
        private Long skuId;
        private String skuCode;
        private Long materialId;
        private String materialCode;
        private String materialName;
        private BigDecimal quantity;
        private String batchNo;
        private String remark;
    }
}
