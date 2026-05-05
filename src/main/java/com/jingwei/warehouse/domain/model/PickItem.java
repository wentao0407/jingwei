package com.jingwei.warehouse.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 拣货项实体
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_warehouse_pick_item")
public class PickItem extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 拣货单ID */
    private Long pickListId;

    /** 出库单行ID */
    private Long outboundLineId;

    /** SKU ID */
    private Long skuId;

    /** 库位ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 计划拣货数量 */
    private BigDecimal plannedQty;

    /** 实际拣货数量 */
    private BigDecimal actualQty;

    /** 状态 */
    private PickItemStatus status;

    /** 备注 */
    private String remark;
}
