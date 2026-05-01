package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 属性定义 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class AttributeDefVO {

    /** 属性定义ID */
    private Long id;

    /** 属性编码 */
    private String code;

    /** 属性名称 */
    private String name;

    /** 适用物料类型 */
    private String materialType;

    /** 输入类型 */
    private String inputType;

    /** 是否必填 */
    private Boolean required;

    /** 排序号 */
    private Integer sortOrder;

    /** 选项列表（SELECT 类型用） */
    private List<String> options;

    /** JSONB 中对应路径 */
    private String extJsonPath;
}
