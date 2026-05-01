package com.jingwei.master.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.config.JsonbTypeHandler;
import com.jingwei.common.domain.model.BaseEntity;
import com.jingwei.common.domain.model.CommonStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 物料主数据实体
 * <p>
 * 对应数据库表 t_md_material。
 * 采用混合模型：公共字段（code, name, type, category_id, unit, status）为独立列，
 * 差异化属性（克重、门幅、成分等）存入 ext_attrs JSONB 字段。
 * </p>
 * <p>
 * ext_attrs 按物料类型存储不同结构：
 * <ul>
 *   <li>FABRIC — {weight, width, composition, yarnCount, weaveType, shrinkage, colorFastness}</li>
 *   <li>TRIM — {spec, material, color, finishingType, applicableStyle}</li>
 *   <li>PACKAGING — {spec, material, thickness, loadBearing}</li>
 * </ul>
 * </p>
 * <p>
 * 物料编码由编码规则引擎自动生成，不可手动修改。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName(value = "t_md_material", autoResultMap = true)
public class Material extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 物料编码（编码规则自动生成，不可手动修改） */
    private String code;

    /** 物料名称 */
    private String name;

    /** 物料类型：FABRIC/TRIM/PACKAGING（不含 PRODUCT） */
    private MaterialType type;

    /** 物料分类ID（外键关联 t_md_category） */
    private Long categoryId;

    /** 基本单位（米/个/套/件） */
    private String unit;

    /** 状态：ACTIVE/INACTIVE */
    private CommonStatus status;

    /** 扩展属性（JSONB，按 type 不同存不同结构，使用 JsonbTypeHandler 避免 PostgreSQL 类型不匹配） */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> extAttrs;

    /** 备注 */
    private String remark;
}
