package com.jingwei.master.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 仓库分页查询请求 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class WarehouseQueryDTO {

    private Long current = 1L;
    private Long size = 10L;
    private String type;
    private String status;
    private String keyword;
}
