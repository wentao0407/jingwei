package com.jingwei.order.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建生产订单行 DTO
 *
 * @author JingWei
 */
@Data
public class ProductionOrderLineCreateDTO {

    /** 款式ID */
    @NotNull(message = "款式ID不能为空")
    private Long spuId;

    /** 颜色款ID */
    @NotNull(message = "颜色款ID不能为空")
    private Long colorWayId;

    /** 尺码组ID */
    @NotNull(message = "尺码组ID不能为空")
    private Long sizeGroupId;

    /** 尺码数量列表 */
    @NotNull(message = "尺码矩阵不能为空")
    private List<SizeEntryDTO> sizes;

    /** BOM ID（下达时必须有，创建时可选） */
    private Long bomId;

    /** 是否跳过裁剪（针织类默认true） */
    private Boolean skipCutting;

    /** 行备注 */
    private String remark;
}
