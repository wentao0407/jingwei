package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.config.JsonbTypeHandler;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 属性定义实体
 * <p>
 * 对应数据库表 t_md_attribute_def。
 * 属性定义是前端动态表单的元数据驱动，告诉前端每种物料类型需要渲染哪些属性字段。
 * ext_json_path 字段对应 ext_attrs JSONB 中的路径，后端据此校验必填项。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName(value = "t_md_attribute_def", autoResultMap = true)
public class AttributeDef extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 属性编码（如 fabric_weight） */
    private String code;

    /** 属性名称（如 面料克重） */
    private String name;

    /** 适用物料类型：FABRIC/TRIM/PACKAGING */
    private MaterialType materialType;

    /** 输入类型：TEXT/NUMBER/SELECT/MULTI_SELECT/COMPOSITION */
    private InputType inputType;

    /** 是否必填 */
    private Boolean required;

    /** 排序号 */
    private Integer sortOrder;

    /** 选项列表（SELECT 类型用，JSONB，使用 JsonbTypeHandler 避免 PostgreSQL 类型不匹配） */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private List<String> options;

    /** JSONB 中对应路径（如 weight） */
    private String extJsonPath;
}
