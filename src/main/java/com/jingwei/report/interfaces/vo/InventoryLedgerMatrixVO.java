package com.jingwei.report.interfaces.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 库存台账矩阵视图 VO（颜色 × 尺码）
 * <p>
 * 用于前端展示矩阵表格，行为颜色，列为尺码。
 * </p>
 *
 * @author JingWei
 */
@Data
public class InventoryLedgerMatrixVO {

    /** 款式ID */
    private Long spuId;

    /** 款式编码 */
    private String spuCode;

    /** 款式名称 */
    private String spuName;

    /** 仓库ID */
    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    /** 尺码列表（矩阵列头，按 sortOrder 排序） */
    private List<String> sizes;

    /** 矩阵数据：colorName → (sizeCode → qty) */
    private Map<String, Map<String, Integer>> matrix;

    /** 颜色行汇总：colorName → totalQty */
    private Map<String, Integer> colorTotals;

    /** 尺⬤列汇总：sizeCode → totalQty */
    private Map<String, Integer> sizeTotals;

    /** 总数量 */
    private Integer grandTotal;
}
