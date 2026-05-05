package com.jingwei.report.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 出入库流水查询 DTO
 *
 * @author JingWei
 */
@Data
public class OperationFlowQueryDTO {

    /** 当前页 */
    private Long current = 1L;

    /** 每页大小 */
    private Long size = 20L;

    /** 库存类型：SKU/MATERIAL */
    private String inventoryType;

    /** 操作类型（OperationType 枚举 code） */
    private String operationType;

    /** 仓库ID */
    private Long warehouseId;

    /** SKU ID */
    private Long skuId;

    /** 物料ID */
    private Long materialId;

    /** 来源单据类型 */
    private String sourceType;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 操作单号模糊搜索 */
    private String operationNo;
}
