package com.jingwei.report.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 畅滞销分析 VO
 *
 * @author JingWei
 */
@Data
public class TurnoverAnalysisVO {

    /** SKU编码 */
    private String skuCode;

    /** 物料编码 */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 款式编码 */
    private String spuCode;

    /** 款式名称 */
    private String spuName;

    /** 颜色名称 */
    private String colorName;

    /** 尺码编码 */
    private String sizeCode;

    /** 仓库名称 */
    private String warehouseName;

    /** 当前库存数量 */
    private BigDecimal currentQty;

    /** 统计期间出库数量 */
    private BigDecimal outboundQty;

    /** 统计期间入库数量 */
    private BigDecimal inboundQty;

    /** 统计期间净出库 = 出库 - 入库 */
    private BigDecimal netOutboundQty;

    /** 平均库存 = (期初库存 + 期末库存) / 2 */
    private BigDecimal avgInventory;

    /** 库存周转天数 = 统计天数 × 平均库存 / 出库数量 */
    private BigDecimal turnoverDays;

    /** 库存周转率 = 出库数量 / 平均库存 */
    private BigDecimal turnoverRate;

    /** 畅销等级：FAST(周转天数<=15) / NORMAL(16-60) / SLOW(61-90) / DEAD(>90) */
    private String turnoverGrade;

    /** 畅销等级标签 */
    private String turnoverGradeLabel;

    /** 统计开始日期 */
    private LocalDate startDate;

    /** 统计结束日期 */
    private LocalDate endDate;

    /** 统计天数 */
    private Integer periodDays;
}
