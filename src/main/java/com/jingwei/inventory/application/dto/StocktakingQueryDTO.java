package com.jingwei.inventory.application.dto;

import lombok.Data;

/**
 * 盘点单查询 DTO
 *
 * @author JingWei
 */
@Data
public class StocktakingQueryDTO {

    /** 当前页 */
    private Long current = 1L;
    /** 每页大小 */
    private Long size = 20L;
    /** 状态筛选 */
    private String status;
    /** 仓库ID筛选 */
    private Long warehouseId;
}
