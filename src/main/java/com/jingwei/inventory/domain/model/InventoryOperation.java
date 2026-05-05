package com.jingwei.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存操作记录实体
 * <p>
 * 对应数据库表 t_inventory_operation，每次库存变更写一条记录。
 * 记录变更前后的四组快照（available/locked/qc/total 的 before/after），
 * 确保所有库存变动可追溯、可校验、可对账。
 * </p>
 * <p>
 * 此实体不继承 BaseEntity，因为操作记录不可修改（无 version/deleted），
 * 仅有 created_at 记录创建时间。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_inventory_operation")
public class InventoryOperation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 操作单号 */
    private String operationNo;

    /** 操作类型 */
    private OperationType operationType;

    /** 库存类型：SKU/MATERIAL */
    private InventoryType inventoryType;

    /** 库存记录ID */
    private Long inventoryId;

    /** SKU ID（成品时使用） */
    private Long skuId;

    /** 物料ID（原料时使用） */
    private Long materialId;

    /** 仓库ID */
    private Long warehouseId;

    /** 库位ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 操作数量（正数） */
    private BigDecimal quantity;

    /** 操作前可用数量 */
    private BigDecimal availableBefore;
    /** 操作后可用数量 */
    private BigDecimal availableAfter;
    /** 操作前锁定数量 */
    private BigDecimal lockedBefore;
    /** 操作后锁定数量 */
    private BigDecimal lockedAfter;
    /** 操作前质检数量 */
    private BigDecimal qcBefore;
    /** 操作后质检数量 */
    private BigDecimal qcAfter;
    /** 操作前实际库存 */
    private BigDecimal totalBefore;
    /** 操作后实际库存 */
    private BigDecimal totalAfter;

    /** 来源单据类型 */
    private String sourceType;
    /** 来源单据ID */
    private Long sourceId;
    /** 来源单据编号 */
    private String sourceNo;

    /** 本次操作的单位成本 */
    private BigDecimal unitCost;
    /** 本次操作的成本金额 = quantity × unit_cost */
    private BigDecimal costAmount;

    /** 操作人ID */
    private Long operatorId;
    /** 操作时间 */
    private LocalDateTime operatedAt;
    /** 备注 */
    private String remark;
    /** 记录创建时间 */
    private LocalDateTime createdAt;
}
