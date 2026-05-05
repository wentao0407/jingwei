package com.jingwei.warehouse.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 收货作业单聚合根
 * <p>
 * 对应数据库表 t_warehouse_receiving。
 * 收货单从 ASN（到货通知单）创建，逐行核对物料并填写实收数量。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_warehouse_receiving")
public class ReceivingOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 收货单号 */
    private String receivingNo;

    /** 到货通知单ID */
    private Long asnId;

    /** 收货仓库ID */
    private Long warehouseId;

    /** 收货日期 */
    private LocalDate receivingDate;

    /** 状态 */
    private ReceivingStatus status;

    /** 收货人ID */
    private Long receiverId;

    /** 收货月台号 */
    private String dockNo;

    /** 关联的入库单ID */
    private Long inboundOrderId;

    /** 备注 */
    private String remark;

    /** 收货行列表（非数据库字段） */
    @TableField(exist = false)
    private List<ReceivingLine> lines = new ArrayList<>();
}
