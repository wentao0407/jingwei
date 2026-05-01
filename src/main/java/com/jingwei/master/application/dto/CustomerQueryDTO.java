package com.jingwei.master.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 客户分页查询请求 DTO
 * <p>
 * 所有筛选条件均为可选，不传则不筛选。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class CustomerQueryDTO {

    /** 当前页码（默认1） */
    private Long current = 1L;

    /** 每页条数（默认10） */
    private Long size = 10L;

    /** 客户类型：WHOLESALE/RETAIL/ONLINE/FRANCHISE（可选） */
    private String type;

    /** 客户等级：A/B/C/D（可选） */
    private String level;

    /** 状态：ACTIVE/INACTIVE（可选） */
    private String status;

    /** 关键词（搜索编码或名称，可选） */
    private String keyword;
}
