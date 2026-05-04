package com.jingwei.procurement.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建采购订单 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateProcurementOrderDTO {

    /** 供应商ID */
    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    /** 订单日期 */
    private String orderDate;

    /** 要求交货日期 */
    private String expectedDeliveryDate;

    /** 备注 */
    private String remark;

    /** 采购订单行 */
    @NotEmpty(message = "采购订单行不能为空")
    private List<ProcurementOrderLineCreateDTO> lines;

    /**
     * 采购订单行创建 DTO
     */
    @Getter
    @Setter
    public static class ProcurementOrderLineCreateDTO {
        private Long materialId;
        private String materialType;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal unitPrice;
        private Long mrpResultId;
        private String remark;
    }
}
