package com.jingwei.procurement.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建到货通知单 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateAsnDTO {

    /** 采购订单ID */
    @NotNull(message = "采购订单ID不能为空")
    private Long procurementOrderId;

    /** 供应商ID */
    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    /** 预计到货日期 */
    private String expectedArrivalDate;

    /** 备注 */
    private String remark;

    /** 到货行列表 */
    @NotEmpty(message = "到货行列表不能为空")
    private List<AsnLineCreateDTO> lines;

    /**
     * 到货行创建 DTO
     */
    @Getter
    @Setter
    public static class AsnLineCreateDTO {

        /** 采购订单行ID */
        @NotNull(message = "采购订单行ID不能为空")
        private Long procurementLineId;

        /** 物料ID */
        @NotNull(message = "物料ID不能为空")
        private Long materialId;

        /** 预计到货数量 */
        @NotNull(message = "预计到货数量不能为空")
        private BigDecimal expectedQuantity;

        /** 批次号 */
        private String batchNo;

        /** 备注 */
        private String remark;
    }
}
