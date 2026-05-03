package com.jingwei.order.interfaces.vo;

import lombok.Data;

import java.util.Map;

/**
 * 生产订单行 VO
 *
 * @author JingWei
 */
@Data
public class ProductionOrderLineVO {

    /** 行ID */
    private Long id;

    /** 行号 */
    private Integer lineNo;

    /** 款式ID */
    private Long spuId;

    /** 款式编码 */
    private String spuCode;

    /** 款式名称 */
    private String spuName;

    /** 颜色款ID */
    private Long colorWayId;

    /** 颜色名称 */
    private String colorName;

    /** 颜色编码 */
    private String colorCode;

    /** BOM ID */
    private Long bomId;

    /** 尺码矩阵（展开为 Map 前端可直接使用） */
    private Map<String, Object> sizeMatrix;

    /** 本行总数量 */
    private Integer totalQuantity;

    /** 已完工数量 */
    private Integer completedQuantity;

    /** 已入库数量 */
    private Integer stockedQuantity;

    /** 是否跳过裁剪 */
    private Boolean skipCutting;

    /** 行状态 */
    private String status;

    /** 行状态中文标签 */
    private String statusLabel;

    /** 行备注 */
    private String remark;
}
