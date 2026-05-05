package com.jingwei.inventory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建盘点单 DTO
 *
 * @author JingWei
 */
@Data
public class CreateStocktakingDTO {

    /** 盘点类型：FULL/CYCLE/SAMPLE/DYNAMIC */
    @NotBlank(message = "盘点类型不能为空")
    private String stocktakingType;

    /** 盘点模式：OPEN=明盘/BLIND=盲盘 */
    @NotBlank(message = "盘点模式不能为空")
    private String countMode;

    /** 盘点仓库ID */
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    /** 盘点库区（可选） */
    private String zoneCode;

    /** 计划盘点日期 */
    private String plannedDate;

    /** 备注 */
    @Size(max = 500, message = "备注不能超过500字")
    private String remark;
}
