package com.jingwei.procurement.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * BOM 行创建 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class BomItemCreateDTO {

    /** 物料ID */
    @NotNull(message = "物料ID不能为空")
    private Long materialId;

    /** 物料类型：FABRIC/TRIM/PACKAGING */
    @NotBlank(message = "物料类型不能为空")
    private String materialType;

    /** 消耗类型：FIXED_PER_PIECE/SIZE_DEPENDENT/PER_ORDER */
    @NotBlank(message = "消耗类型不能为空")
    private String consumptionType;

    /** 基准用量 */
    @NotNull(message = "基准用量不能为空")
    private BigDecimal baseConsumption;

    /** 基准尺码ID（SIZE_DEPENDENT 时必填） */
    private Long baseSizeId;

    /** 用量单位 */
    @NotBlank(message = "用量单位不能为空")
    private String unit;

    /** 损耗率（面料专用，如0.08表示8%） */
    private BigDecimal wastageRate;

    /** 尺码用量列表（SIZE_DEPENDENT 时必填） */
    private List<SizeConsumptionDTO> sizeConsumptions;

    /** 行备注 */
    private String remark;

    /**
     * 尺码用量 DTO
     */
    @Getter
    @Setter
    public static class SizeConsumptionDTO {
        private Long sizeId;
        private String code;
        private BigDecimal consumption;
    }
}
