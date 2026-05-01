package com.jingwei.master.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 尺码组查询条件 DTO
 * <p>
 * 支持按品类和状态筛选尺码组列表。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
public class SizeGroupQueryDTO {

    /** 适用品类（WOMEN/MEN/CHILDREN），null 表示不限 */
    private String category;

    /** 状态（ACTIVE/INACTIVE），null 表示不限 */
    private String status;
}
