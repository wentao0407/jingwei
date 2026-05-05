package com.jingwei.order.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 退货单行展示 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class ReturnOrderLineVO {

    /** 退货行ID */
    private Long id;

    /** 退货单ID */
    private Long returnId;

    /** 原销售订单行ID */
    private Long salesOrderLineId;

    /** 款式ID */
    private Long spuId;

    /** 颜色款ID */
    private Long colorWayId;

    /** 退货尺码矩阵（JSON字符串） */
    private String sizeMatrixJson;

    /** 本行退货数量 */
    private Integer totalQuantity;

    /** 质检合格数量 */
    private Integer qcPassedQty;

    /** 质检不合格数量 */
    private Integer qcFailedQty;

    /** 质检结果（JSON字符串） */
    private String qcResult;

    /** 备注 */
    private String remark;
}
