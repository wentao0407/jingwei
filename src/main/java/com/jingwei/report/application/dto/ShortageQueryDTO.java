package com.jingwei.report.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 缺货统计查询 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ShortageQueryDTO {

    /** 当前页 */
    private Long current = 1L;

    /** 每页大小 */
    private Long size = 20L;

    /** 款式ID（可选筛选） */
    private Long spuId;

    /** 客户ID（可选筛选） */
    private Long customerId;

    /** 关键字（款式编码/名称/订单号） */
    private String keyword;
}
