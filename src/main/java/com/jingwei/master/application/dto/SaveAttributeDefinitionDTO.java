package com.jingwei.master.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 创建/编辑属性定义 DTO
 *
 * @author JingWei
 */
@Getter
@Setter
public class SaveAttributeDefinitionDTO {

    /** 属性编码 */
    @NotBlank(message = "属性编码不能为空")
    private String code;

    /** 属性名称 */
    @NotBlank(message = "属性名称不能为空")
    private String name;

    /** 适用物料类型 */
    @NotBlank(message = "物料类型不能为空")
    private String materialType;

    /** 输入类型 */
    @NotBlank(message = "输入类型不能为空")
    private String inputType;

    /** 是否必填 */
    @NotNull(message = "是否必填不能为空")
    private Boolean required;

    /** 排序号 */
    private Integer sortOrder;

    /** 选项列表 */
    private List<String> options;

    /** JSONB 路径 */
    private String jsonbPath;

    /** 备注 */
    private String remark;
}
