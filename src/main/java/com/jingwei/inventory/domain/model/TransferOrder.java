package com.jingwei.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 调拨单聚合根
 * <p>
 * 对应数据库表 t_inventory_transfer，管理跨仓库库存调拨。
 * 流程：DRAFT → CONFIRMED → IN_TRANSIT → COMPLETED
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_inventory_transfer")
public class TransferOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 调拨单号 */
    private String transferNo;

    /** 源仓库ID */
    private Long sourceWarehouseId;

    /** 目标仓库ID */
    private Long targetWarehouseId;

    /** 状态 */
    private TransferStatus status;

    /** 备注 */
    private String remark;

    /** 调拨单行列表（非数据库字段） */
    @TableField(exist = false)
    private List<TransferOrderLine> lines = new ArrayList<>();
}
