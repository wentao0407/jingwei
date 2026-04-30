package com.jingwei.system.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 角色分页查询DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class RoleQueryDTO {

    /** 当前页码，默认1 */
    private long current = 1;

    /** 每页大小，默认10 */
    private long size = 10;

    /** 搜索关键词（角色编码/角色名称） */
    private String keyword;

    /** 状态筛选 */
    private String status;
}
