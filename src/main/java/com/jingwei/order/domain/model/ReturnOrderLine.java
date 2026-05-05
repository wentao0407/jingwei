package com.jingwei.order.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 退货单行
 * <p>
 * 对应数据库表 t_order_return_line，记录每个颜色款的退货尺码矩阵。
 * 退货入库时由系统解析 size_matrix JSONB 将矩阵拆解为 SKU 级别的库存操作。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_order_return_line")
public class ReturnOrderLine extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 退货单ID */
    private Long returnId;

    /** 原销售订单行ID */
    private Long salesOrderLineId;

    /** 款式ID（外键→t_md_spu） */
    private Long spuId;

    /** 颜色款ID（外键→t_md_color_way） */
    private Long colorWayId;

    /** 退货尺码矩阵（JSONB，同销售订单行结构） */
    @TableField(typeHandler = SizeMatrixTypeHandler.class)
    private SizeMatrix sizeMatrix;

    /** 本行退货数量（所有尺码求和） */
    private Integer totalQuantity;

    /** 质检合格数量 */
    private Integer qcPassedQty;

    /** 质检不合格数量 */
    private Integer qcFailedQty;

    /** 质检结果（JSONB，记录每个SKU的质检详情） */
    private String qcResult;

    /** 备注 */
    private String remark;
}
