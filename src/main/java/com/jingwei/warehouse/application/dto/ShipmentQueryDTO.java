package com.jingwei.warehouse.application.dto;

import lombok.Data;

/**
 * 发运单查询 DTO。
 *
 * 当前发运聚合以已创建的出库单为基础，补充发运确认动作。
 */
@Data
public class ShipmentQueryDTO {

    /** 当前页 */
    private Long current = 1L;
    /** 每页大小 */
    private Long size = 20L;
    /** 出库/发运状态 */
    private String status;
    /** 仓库ID */
    private Long warehouseId;
    /** 出库单号 */
    private String outboundNo;
}
