package com.jingwei.procurement.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.config.JsonbTypeHandler;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 到货通知单行
 * <p>
 * 对应数据库表 t_procurement_asn_line。
 * 每行对应一个采购订单行的到货信息，支持部分收货和检验结果记录。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName(value = "t_procurement_asn_line", autoResultMap = true)
public class AsnLine extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 到货通知单ID */
    private Long asnId;

    /** 采购订单行ID */
    private Long procurementLineId;

    /** 物料ID */
    private Long materialId;

    /** 预计到货数量 */
    private BigDecimal expectedQuantity;

    /** 实收数量 */
    private BigDecimal receivedQuantity;

    /** 检验状态 */
    private QcStatus qcStatus;

    /** 检验合格数量 */
    private BigDecimal acceptedQuantity;

    /** 检验不合格数量 */
    private BigDecimal rejectedQuantity;

    /** 批次号 */
    private String batchNo;

    /** 检验结果详情（JSONB） */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private QcResult qcResult;

    /** 备注 */
    private String remark;
}
