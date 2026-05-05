package com.jingwei.inventory.application.dto;

import lombok.Data;

/**
 * 预警查询 DTO
 *
 * @author JingWei
 */
@Data
public class AlertQueryDTO {

    /** 状态筛选（ACTIVE/ACKNOWLEDGED/RESOLVED） */
    private String status;
}
