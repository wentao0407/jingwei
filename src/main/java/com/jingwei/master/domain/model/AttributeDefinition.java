package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.config.JsonbTypeHandler;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 物料属性定义聚合根
 * <p>
 * 管理物料扩展属性的元数据，驱动前端动态表单。
 * 对应数据库表 t_master_attribute_definition。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName(value = "t_master_attribute_definition", autoResultMap = true)
public class AttributeDefinition extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 属性编码（唯一） */
    private String code;

    /** 属性名称 */
    private String name;

    /** 适用物料类型（FABRIC/ACCESSORY/PACKAGING） */
    private String materialType;

    /** 输入类型（TEXT/NUMBER/SELECT/MULTI_SELECT/COMPONENT） */
    private String inputType;

    /** 是否必填 */
    private Boolean required;

    /** 排序号 */
    private Integer sortOrder;

    /** 选项列表（SELECT/MULTI_SELECT 时使用，JSONB 数组） */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private List<String> options;

    /** 在物料扩展属性 JSONB 中的路径 */
    private String jsonbPath;

    /** 备注 */
    private String remark;
}
