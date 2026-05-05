package com.jingwei.report.application.dto;

import lombok.Data;

/**
 * 库存台账查询 DTO
 *
 * @author JingWei
 */
@Data
public class InventoryLedgerQueryDTO {

    /** 当前页 */
    private Long current = 1L;

    /** 每页大小 */
    private Long size = 20L;

    /** 库存类型：SKU/MATERIAL */
    private String inventoryType;

    /** 仓库ID */
    private Long warehouseId;

    /** 品类ID（成品按 SPU 品类筛选） */
    private Long categoryId;

    /** 季节ID（成品按 SPU 季节筛选） */
    private Long seasonId;

    /** SKU ID（精确查询） */
    private Long skuId;

    /** 物料ID（精确查询） */
    private Long materialId;

    /** SKU编码/物料编码模糊搜索 */
    private String keyword;
}
