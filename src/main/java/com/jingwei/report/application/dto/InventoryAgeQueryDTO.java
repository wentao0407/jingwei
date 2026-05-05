package com.jingwei.report.application.dto;

import lombok.Data;

/**
 * 库龄分析查询 DTO
 *
 * @author JingWei
 */
@Data
public class InventoryAgeQueryDTO {

    /** 当前页 */
    private Long current = 1L;

    /** 每页大小 */
    private Long size = 20L;

    /** 库存类型：SKU/MATERIAL */
    private String inventoryType;

    /** 仓库ID */
    private Long warehouseId;

    /** 品类ID */
    private Long categoryId;

    /** 季节ID */
    private Long seasonId;

    /** 关键字搜索 */
    private String keyword;
}
