package com.jingwei.procurement.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.config.JsonbTypeHandler;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

/**
 * MRP 计算来源
 * <p>
 * 对应数据库表 t_procurement_mrp_source。
 * 追溯每个需求来自哪个生产订单、哪个颜色款、哪个尺码。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName(value = "t_procurement_mrp_source", autoResultMap = true)
public class MrpSource extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 计算批次号 */
    private String batchNo;

    /** MRP结果ID */
    private Long resultId;

    /** 生产订单ID */
    private Long productionOrderId;

    /** 生产订单行ID */
    private Long productionLineId;

    /** 款式ID */
    private Long spuId;

    /** 颜色款ID */
    private Long colorWayId;

    /** 物料ID */
    private Long materialId;

    /** 使用的BOM版本ID */
    private Long bomId;

    /** 本来源的需求量 */
    private BigDecimal demandQuantity;

    /** 需求明细（各尺码的计算过程，JSONB） */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> detail;
}
