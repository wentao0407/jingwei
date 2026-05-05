package com.jingwei.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 在途库存实体
 * <p>
 * 对应数据库表 t_inventory_in_transit，按采购订单行跟踪在途物料。
 * 在途库存不在仓库物理空间内，仅为数量跟踪。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_inventory_in_transit")
public class InventoryInTransit extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 采购订单ID */
    private Long procurementOrderId;

    /** 采购订单行ID */
    private Long procurementLineId;

    /** 物料ID */
    private Long materialId;

    /** 目标仓库ID */
    private Long warehouseId;

    /** 预计到货数量 */
    private BigDecimal expectedQty;

    /** 已收货数量 */
    private BigDecimal receivedQty;

    /** 剩余在途 = expected - received */
    private BigDecimal remainingQty;

    /** 预计到货日期 */
    private LocalDate expectedArrivalDate;

    /** 状态：PENDING/PARTIAL_RECEIVED/FULLY_RECEIVED */
    private InTransitStatus status;
}
