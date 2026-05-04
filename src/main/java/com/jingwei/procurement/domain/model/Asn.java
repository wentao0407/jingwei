package com.jingwei.procurement.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 到货通知单聚合根
 * <p>
 * 对应数据库表 t_procurement_asn。
 * 采购订单下发后，供应商发货时创建到货通知单，仓库收货后进行来料检验。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_procurement_asn")
public class Asn extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 到货通知单号 */
    private String asnNo;

    /** 采购订单ID */
    private Long procurementOrderId;

    /** 供应商ID */
    private Long supplierId;

    /** 预计到货日期 */
    private LocalDate expectedArrivalDate;

    /** 实际到货日期 */
    private LocalDate actualArrivalDate;

    /** 状态 */
    private AsnStatus status;

    /** 收货人ID */
    private Long receiverId;

    /** 备注 */
    private String remark;

    /** 到货通知单行列表（非数据库字段） */
    @TableField(exist = false)
    private List<AsnLine> lines = new ArrayList<>();
}
