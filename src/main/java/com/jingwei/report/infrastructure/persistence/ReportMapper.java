package com.jingwei.report.infrastructure.persistence;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

/**
 * 报表查询 Mapper
 * <p>
 * 使用原生 SQL 实现跨表关联查询，不走 MyBatis-Plus 的 BaseMapper。
 * 动态条件查询使用 @SelectProvider 避免 XML 解析 text block 的问题。
 * </p>
 *
 * @author JingWei
 */
@Mapper
public interface ReportMapper {

    /**
     * 成品库存台账分页查询
     */
    @SelectProvider(type = ReportSqlProvider.class, method = "selectSkuLedgerPage")
    IPage<Map<String, Object>> selectSkuLedgerPage(Page<?> page,
                                                     @Param("warehouseId") Long warehouseId,
                                                     @Param("skuId") Long skuId,
                                                     @Param("categoryId") Long categoryId,
                                                     @Param("seasonId") Long seasonId,
                                                     @Param("keyword") String keyword);

    /**
     * 原料库存台账分页查询
     */
    @SelectProvider(type = ReportSqlProvider.class, method = "selectMaterialLedgerPage")
    IPage<Map<String, Object>> selectMaterialLedgerPage(Page<?> page,
                                                         @Param("warehouseId") Long warehouseId,
                                                         @Param("materialId") Long materialId,
                                                         @Param("keyword") String keyword);

    /**
     * 成品库存台账导出（不分页，带筛选）
     */
    @SelectProvider(type = ReportSqlProvider.class, method = "selectSkuLedgerExport")
    List<Map<String, Object>> selectSkuLedgerExport(@Param("warehouseId") Long warehouseId,
                                                     @Param("skuId") Long skuId,
                                                     @Param("categoryId") Long categoryId,
                                                     @Param("seasonId") Long seasonId,
                                                     @Param("keyword") String keyword);

    /**
     * 原料库存台账导出（不分页，带筛选）
     */
    @SelectProvider(type = ReportSqlProvider.class, method = "selectMaterialLedgerExport")
    List<Map<String, Object>> selectMaterialLedgerExport(@Param("warehouseId") Long warehouseId,
                                                          @Param("materialId") Long materialId,
                                                          @Param("keyword") String keyword);

    /**
     * 出入库流水（库存操作记录）分页查询
     */
    @SelectProvider(type = ReportSqlProvider.class, method = "selectOperationFlowPage")
    IPage<Map<String, Object>> selectOperationFlowPage(Page<?> page,
                                                        @Param("inventoryType") String inventoryType,
                                                        @Param("operationType") String operationType,
                                                        @Param("warehouseId") Long warehouseId,
                                                        @Param("skuId") Long skuId,
                                                        @Param("materialId") Long materialId,
                                                        @Param("sourceType") String sourceType,
                                                        @Param("startTime") java.time.LocalDateTime startTime,
                                                        @Param("endTime") java.time.LocalDateTime endTime,
                                                        @Param("operationNo") String operationNo);

    /**
     * 出入库流水导出（不分页，带筛选）
     */
    @SelectProvider(type = ReportSqlProvider.class, method = "selectOperationFlowExport")
    List<Map<String, Object>> selectOperationFlowExport(@Param("inventoryType") String inventoryType,
                                                         @Param("operationType") String operationType,
                                                         @Param("warehouseId") Long warehouseId,
                                                         @Param("skuId") Long skuId,
                                                         @Param("materialId") Long materialId,
                                                         @Param("sourceType") String sourceType,
                                                         @Param("startTime") java.time.LocalDateTime startTime,
                                                         @Param("endTime") java.time.LocalDateTime endTime,
                                                         @Param("operationNo") String operationNo);

    /**
     * 查询某款式在指定仓库的库存矩阵数据（颜色 × 尺码）
     */
    @Select("""
            SELECT cw.color_name AS colorName,
                   sz.code AS sizeCode,
                   sz.sort_order AS sortOrder,
                   COALESCE(inv.total_qty, 0) AS totalQty
            FROM t_md_sku sku
            JOIN t_md_color_way cw ON cw.id = sku.color_way_id AND cw.deleted = FALSE
            JOIN t_md_size sz ON sz.id = sku.size_id AND sz.deleted = FALSE
            LEFT JOIN t_inventory_sku inv ON inv.sku_id = sku.id
                AND inv.warehouse_id = #{warehouseId}
                AND inv.deleted = FALSE
            WHERE sku.spu_id = #{spuId}
              AND sku.deleted = FALSE
            ORDER BY cw.sort_order, sz.sort_order
            """)
    List<Map<String, Object>> selectSkuMatrix(@Param("spuId") Long spuId,
                                               @Param("warehouseId") Long warehouseId);

    // ==================== 库龄分析 ====================

    /**
     * 成品库龄分析查询
     */
    @SelectProvider(type = ReportSqlProvider.class, method = "selectSkuAgePage")
    IPage<Map<String, Object>> selectSkuAgePage(Page<?> page,
                                                  @Param("warehouseId") Long warehouseId,
                                                  @Param("categoryId") Long categoryId,
                                                  @Param("seasonId") Long seasonId,
                                                  @Param("keyword") String keyword);

    /**
     * 原料库龄分析查询
     */
    @SelectProvider(type = ReportSqlProvider.class, method = "selectMaterialAgePage")
    IPage<Map<String, Object>> selectMaterialAgePage(Page<?> page,
                                                       @Param("warehouseId") Long warehouseId,
                                                       @Param("keyword") String keyword);

    // ==================== 畅滞销分析 ====================

    /**
     * 成品畅滞销分析查询
     */
    @SelectProvider(type = ReportSqlProvider.class, method = "selectSkuTurnoverPage")
    IPage<Map<String, Object>> selectSkuTurnoverPage(Page<?> page,
                                                       @Param("warehouseId") Long warehouseId,
                                                       @Param("categoryId") Long categoryId,
                                                       @Param("seasonId") Long seasonId,
                                                       @Param("startDate") java.time.LocalDate startDate,
                                                       @Param("endDate") java.time.LocalDate endDate,
                                                       @Param("keyword") String keyword);

    /**
     * 原料畅滞销分析查询
     */
    @SelectProvider(type = ReportSqlProvider.class, method = "selectMaterialTurnoverPage")
    IPage<Map<String, Object>> selectMaterialTurnoverPage(Page<?> page,
                                                            @Param("warehouseId") Long warehouseId,
                                                            @Param("startDate") java.time.LocalDate startDate,
                                                            @Param("endDate") java.time.LocalDate endDate,
                                                            @Param("keyword") String keyword);
}
