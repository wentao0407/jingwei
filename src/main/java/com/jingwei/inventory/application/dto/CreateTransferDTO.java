package com.jingwei.inventory.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建调拨单 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateTransferDTO {

    /** 源仓库ID */
    @NotNull(message = "源仓库ID不能为空")
    private Long sourceWarehouseId;

    /** 目标仓库ID */
    @NotNull(message = "目标仓库ID不能为空")
    private Long targetWarehouseId;

    /** 备注 */
    private String remark;

    /** 调拨行 */
    @NotEmpty(message = "调拨行不能为空")
    private List<TransferLineDTO> lines;

    @Getter
    @Setter
    public static class TransferLineDTO {
        /** 库存类型：SKU / MATERIAL */
        @NotNull(message = "库存类型不能为空")
        private String inventoryType;
        /** SKU ID */
        private Long skuId;
        /** 物料 ID */
        private Long materialId;
        /** 调拨数量 */
        @NotNull(message = "调拨数量不能为空")
        private BigDecimal quantity;
        /** 批次号 */
        private String batchNo;
        /** 备注 */
        private String remark;
    }
}
