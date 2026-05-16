package com.jingwei.warehouse.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateMaterialReturnDTO {
    @NotNull(message = "生产订单ID不能为空")
    private Long productionOrderId;
    private String remark;

    @NotEmpty(message = "退料行不能为空")
    private List<MaterialReturnLineDTO> lines;

    @Getter
    @Setter
    public static class MaterialReturnLineDTO {
        @NotNull(message = "物料ID不能为空")
        private Long materialId;
        private String batchNo;
        @NotNull(message = "数量不能为空")
        private BigDecimal quantity;
        private String unit;
        private String remark;
    }
}
