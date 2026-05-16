package com.jingwei.inventory.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 调拨单行
 * <p>
 * 对应数据库表 t_inventory_transfer_line。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_inventory_transfer_line")
public class TransferOrderLine extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 调拨单ID */
    private Long transferId;

    /** 库存类型：SKU / MATERIAL */
    private String inventoryType;

    /** SKU ID（inventory_type=SKU 时） */
    private Long skuId;

    /** 物料 ID（inventory_type=MATERIAL 时） */
    private Long materialId;

    /** 调拨数量 */
    private BigDecimal quantity;

    /** 批次号 */
    private String batchNo;

    /** 备注 */
    private String remark;
}
