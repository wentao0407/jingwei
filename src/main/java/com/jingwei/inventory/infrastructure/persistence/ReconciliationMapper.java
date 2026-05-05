package com.jingwei.inventory.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jingwei.inventory.domain.model.ReconciliationAnomaly;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 库存对账 Mapper
 *
 * @author JingWei
 */
@Mapper
public interface ReconciliationMapper extends BaseMapper<ReconciliationAnomaly> {

    /**
     * 查询成品库存当日操作流水净变动（按 inventory_id 汇总）
     * <p>
     * 净变动 = 入库量 - 出库量 - 盘亏 + 盘盈
     * ALLOCATE 和 RELEASE 不影响 total，不计入。
     * </p>
     *
     * @param accountDate 账期
     * @return 每条库存记录的净变动（inventory_id → net_change）
     */
    @Select("SELECT io.inventory_id, " +
            "  SUM(CASE " +
            "    WHEN io.operation_type IN ('INBOUND_PURCHASE','INBOUND_PRODUCTION','INBOUND_RETURN','ADJUST_GAIN') " +
            "      THEN io.quantity ELSE 0 " +
            "  END) - " +
            "  SUM(CASE " +
            "    WHEN io.operation_type IN ('OUTBOUND_SALES','OUTBOUND_MATERIAL','ADJUST_LOSS') " +
            "      THEN io.quantity ELSE 0 " +
            "  END) AS net_change " +
            "FROM t_inventory_operation io " +
            "WHERE io.operated_at >= #{accountDate}::timestamp " +
            "  AND io.operated_at < (#{accountDate}::date + INTERVAL '1 day')::timestamp " +
            "  AND io.deleted = FALSE " +
            "GROUP BY io.inventory_id")
    List<Object[]> selectSkuOpsNetChangeByDate(@Param("accountDate") LocalDate accountDate);

    /**
     * 查询原料库存当日操作流水净变动（按 inventory_id 汇总）
     */
    @Select("SELECT io.inventory_id, " +
            "  SUM(CASE " +
            "    WHEN io.operation_type IN ('INBOUND_PURCHASE','INBOUND_PRODUCTION','INBOUND_RETURN','ADJUST_GAIN') " +
            "      THEN io.quantity ELSE 0 " +
            "  END) - " +
            "  SUM(CASE " +
            "    WHEN io.operation_type IN ('OUTBOUND_SALES','OUTBOUND_MATERIAL','ADJUST_LOSS') " +
            "      THEN io.quantity ELSE 0 " +
            "  END) AS net_change " +
            "FROM t_inventory_operation io " +
            "WHERE io.operated_at >= #{accountDate}::timestamp " +
            "  AND io.operated_at < (#{accountDate}::date + INTERVAL '1 day')::timestamp " +
            "  AND io.deleted = FALSE " +
            "  AND io.inventory_type = 'MATERIAL' " +
            "GROUP BY io.inventory_id")
    List<Object[]> selectMaterialOpsNetChangeByDate(@Param("accountDate") LocalDate accountDate);
}
