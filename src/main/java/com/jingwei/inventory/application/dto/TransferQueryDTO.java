package com.jingwei.inventory.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 调拨单查询 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class TransferQueryDTO {

    /** 当前页码 */
    private int current = 1;

    /** 每页大小 */
    private int size = 10;

    /** 状态筛选 */
    private String status;
}
