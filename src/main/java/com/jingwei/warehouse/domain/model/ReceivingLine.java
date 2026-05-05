package com.jingwei.warehouse.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 收货行实体
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_warehouse_receiving_line")
public class ReceivingLine extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 收货单ID */
    private Long receivingId;

    /** 到货通知单行ID */
    private Long asnLineId;

    /** 物料ID */
    private Long materialId;

    /** 预计数量 */
    private BigDecimal expectedQty;

    /** 实收数量 */
    private BigDecimal receivedQty;

    /** 实收卷数（面料专用） */
    private Integer rollCount;

    /** 差异数量 = received - expected */
    private BigDecimal differenceQty;

    /** 差异原因 */
    private String differenceReason;

    /** 收货批次号（系统自动生成） */
    private String batchNo;

    /** 质检状态 */
    private QcLineStatus qcStatus;

    /** 上架状态 */
    private PutawayLineStatus putawayStatus;

    /** 上架库位ID */
    private Long putawayLocationId;

    /** 备注 */
    private String remark;
}
