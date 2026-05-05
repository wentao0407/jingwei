package com.jingwei.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存预留实体
 * <p>
 * 对应数据库表 t_inventory_allocation，记录销售订单锁定的库存。
 * 销售订单确认时创建预留（available → locked），取消时释放（locked → available）。
 * </p>
 * <p>
 * 预留策略：
 * <ul>
 *   <li>全额预留：available >= 需求量 → allocated_qty = 需求量</li>
 *   <li>部分预留：available < 需求量 → allocated_qty = available，记录缺口</li>
 *   <li>无库存：available = 0 → 不创建预留，返回缺货</li>
 * </ul>
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_inventory_allocation")
public class InventoryAllocation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 预留单号 */
    private String allocationNo;

    /** 订单类型：SALES/PRODUCTION */
    private String orderType;

    /** 订单ID */
    private Long orderId;

    /** 订单行ID */
    private Long orderLineId;

    /** SKU ID（成品预留） */
    private Long skuId;

    /** 物料ID（原料预留，领料场景） */
    private Long materialId;

    /** 仓库ID */
    private Long warehouseId;

    /** 批次号（可选，指定批次预留） */
    private String batchNo;

    /** 预留数量 */
    private BigDecimal allocatedQty;

    /** 已出库数量 */
    private BigDecimal fulfilledQty;

    /** 剩余预留 = allocated - fulfilled */
    private BigDecimal remainingQty;

    /** 状态 */
    private AllocationStatus status;

    /** 过期时间（到期自动释放） */
    private LocalDateTime expireAt;

    /** 备注 */
    private String remark;
}
