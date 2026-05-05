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
 * 入库单聚合根
 * <p>
 * 对应数据库表 t_warehouse_inbound，管理各类入库业务。
 * 入库确认时调用 InventoryDomainService.changeInventory() 驱动库存变更。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_warehouse_inbound")
public class InboundOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 入库单号（编码规则生成，格式 RK-年月日-4位流水号） */
    private String inboundNo;

    /** 入库类型 */
    private InboundType inboundType;

    /** 目标仓库ID */
    private Long warehouseId;

    /** 状态 */
    private InboundStatus status;

    /** 来源单据类型（PROCUREMENT_ORDER/PRODUCTION_ORDER/SALES_ORDER/TRANSFER_ORDER） */
    private String sourceType;

    /** 来源单据ID */
    private Long sourceId;

    /** 来源单据编号 */
    private String sourceNo;

    /** 操作人ID */
    private Long operatorId;

    /** 入库日期 */
    private LocalDate inboundDate;

    /** 备注 */
    private String remark;

    /** 入库单行列表（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private List<InboundOrderLine> lines = new ArrayList<>();
}
