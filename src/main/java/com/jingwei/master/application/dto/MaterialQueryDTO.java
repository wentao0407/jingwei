package com.jingwei.master.application.dto;

import lombok.Data;

/**
 * 物料分页查询请求 DTO
 *
 * @author JingWei
 */
@Data
public class MaterialQueryDTO {

    /** 当前页码（默认1） */
    private Long current = 1L;

    /** 每页条数（默认20） */
    private Long size = 20L;

    /** 物料类型筛选：FABRIC/TRIM/PACKAGING */
    private String type;

    /** 物料分类ID筛选 */
    private Long categoryId;

    /** 状态筛选：ACTIVE/INACTIVE */
    private String status;

    /** 关键词搜索（编码或名称模糊匹配） */
    private String keyword;
}
