package com.jingwei.system.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户分页查询DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class UserQueryDTO {

    /** 当前页码，默认1 */
    private long current = 1;

    /** 每页大小，默认10 */
    private long size = 10;

    /** 搜索关键词（用户名/姓名/手机号） */
    private String keyword;

    /** 状态筛选 */
    private String status;
}
