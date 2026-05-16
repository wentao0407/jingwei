package com.jingwei.warehouse.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建领料单 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class CreateMaterialIssueDTO {

    @NotNull(message = "生产订单ID不能为空")
    private Long productionOrderId;

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    private Long productionLineId;

    private String remark;

    @NotEmpty(message = "领料行不能为空")
    private List<MaterialIssueLineDTO> lines;

    @Getter
    @Setter
    public static class MaterialIssueLineDTO {
        @NotNull(message = "物料ID不能为空")
        private Long materialId;
        private String batchNo;
        @NotNull(message = "数量不能为空")
        private BigDecimal quantity;
        private String unit;
        private String remark;
    }
}
