package com.jingwei.inventory.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * 库存查询 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class InventoryStockQueryDTO {

    @Min(value = 1, message = "页码最小为1")
    private Long current = 1L;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Long size = 20L;

    private Long skuId;
    private Long materialId;
    private Long warehouseId;
    private String batchNo;
}
