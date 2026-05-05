package com.jingwei.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 出库单聚合根
 * <p>
 * 对应数据库表 t_warehouse_outbound，管理各类出库业务。
 * 关键规则：只有 SHIPPED 状态时才扣减库存（货物实际离开仓库）。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_warehouse_outbound")
public class OutboundOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 出库单号（编码规则生成，格式 CK-年月日-4位流水号） */
    private String outboundNo;

    /** 出库类型 */
    private OutboundType outboundType;

    /** 源仓库ID */
    private Long warehouseId;

    /** 状态 */
    private OutboundStatus status;

    /** 来源单据类型 */
    private String sourceType;

    /** 来源单据ID */
    private Long sourceId;

    /** 来源单据编号 */
    private String sourceNo;

    /** 操作人ID */
    private Long operatorId;

    /** 出库日期 */
    private LocalDate outboundDate;

    /** 物流公司 */
    private String carrier;

    /** 物流单号 */
    private String trackingNo;

    /** 备注 */
    private String remark;

    /** 出库单行列表（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private List<OutboundOrderLine> lines = new ArrayList<>();
}
