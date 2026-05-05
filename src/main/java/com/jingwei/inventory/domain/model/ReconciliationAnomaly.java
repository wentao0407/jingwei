package com.jingwei.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存对账异常记录实体
 * <p>
 * 日终对账定时任务自动执行，操作流水汇总与库存余额不一致时写入此表。
 * 同一账期同一库存记录只生成一条异常（幂等）。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_inventory_reconciliation_anomaly")
public class ReconciliationAnomaly extends BaseEntity {

    /** 账期（对账执行日期） */
    private LocalDate accountDate;

    /** 库存类型：SKU（成品）/ MATERIAL（原料） */
    private InventoryType inventoryType;

    /** 库存记录ID */
    private Long inventoryId;

    /** 成品SKU ID（inventory_type=SKU 时有值） */
    private Long skuId;

    /** 原料ID（inventory_type=MATERIAL 时有值） */
    private Long materialId;

    /** 仓库ID */
    private Long warehouseId;

    /** 期初实际库存 */
    private BigDecimal totalBefore;

    /** 期末实际库存 */
    private BigDecimal totalAfter;

    /** 当日操作流水汇总净变动 */
    private BigDecimal opsNetChange;

    /** 差异量 = (期末 - 期初) - 流水净变动 */
    private BigDecimal diffQty;

    /** 状态：PENDING / CONFIRMED / IGNORED */
    private String status;

    /** 处理备注 */
    private String remark;

    /** 处理人 */
    private Long resolvedBy;

    /** 处理时间 */
    private LocalDateTime resolvedAt;
}
