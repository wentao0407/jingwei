package com.jingwei.procurement.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 更新采购订单 DTO
 * <p>
 * 仅 DRAFT 状态的采购订单允许编辑。
 * 编辑时会重建行项目（删除旧行 + 插入新行），重新计算总金额。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class UpdateProcurementOrderDTO {

    /** 供应商ID */
    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    /** 订单日期 */
    private String orderDate;

    /** 要求交货日期 */
    private String expectedDeliveryDate;

    /** 备注 */
    private String remark;

    /** 采购订单行（替换整个行列表） */
    @NotNull(message = "采购订单行不能为空")
    private List<ProcurementOrderLineUpdateDTO> lines;

    /**
     * 采购订单行更新 DTO
     */
    @Getter
    @Setter
    public static class ProcurementOrderLineUpdateDTO {
        private Long materialId;
        private String materialType;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal unitPrice;
        private Long mrpResultId;
        private String remark;
    }
}
