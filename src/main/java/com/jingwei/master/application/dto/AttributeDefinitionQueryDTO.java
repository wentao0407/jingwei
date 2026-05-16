package com.jingwei.master.application.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 属性定义查询 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class AttributeDefinitionQueryDTO {

    /** 当前页 */
    private Integer current = 1;

    /** 每页大小 */
    private Integer size = 20;

    /** 物料类型 */
    private String materialType;

    /** 关键字 */
    private String keyword;
}
