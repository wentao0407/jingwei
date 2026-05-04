package com.jingwei.procurement.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.config.JsonbTypeHandler;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * BOM 行项目
 * <p>
 * 对应数据库表 t_bom_item。每行描述一种物料的用量信息。
 * 三种消耗类型决定 MRP 计算时的用量展开方式。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName(value = "t_bom_item", autoResultMap = true)
public class BomItem extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** BOM ID（外键→t_bom） */
    private Long bomId;

    /** 物料ID（外键→t_md_material） */
    private Long materialId;

    /** 物料类型：FABRIC/TRIM/PACKAGING */
    private String materialType;

    /** 消耗类型：FIXED_PER_PIECE/SIZE_DEPENDENT/PER_ORDER */
    private ConsumptionType consumptionType;

    /** 基准用量（FIXED_PER_PIECE: 每件用量; PER_ORDER: 整单用量; SIZE_DEPENDENT: 基准码用量） */
    private BigDecimal baseConsumption;

    /** 基准尺码ID（SIZE_DEPENDENT 时使用） */
    private Long baseSizeId;

    /** 用量单位（米/个/套/张） */
    private String unit;

    /** 损耗率（如0.08表示8%，面料专用） */
    private BigDecimal wastageRate;

    /** 尺码用量表（JSONB，SIZE_DEPENDENT 时使用） */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private SizeConsumptions sizeConsumptions;

    /** 排序号 */
    private Integer sortOrder;

    /** 行备注 */
    private String remark;
}
