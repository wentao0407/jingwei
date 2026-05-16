package com.jingwei.warehouse.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * 波次分页查询 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class WaveQueryDTO {
    @Min(value = 1, message = "页码最小为1")
    private Long current = 1L;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Long size = 20L;

    private Long warehouseId;
    private String status;
    private String waveNo;
}
