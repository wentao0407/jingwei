package com.jingwei.procurement.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MRP 计算结果
 * <p>
 * 对应数据库表 t_procurement_mrp_result。
 * 每次 MRP 计算生成一批结果，记录每个物料的毛需求、可用库存、在途数量、净需求和建议采购量。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_procurement_mrp_result")
public class MrpResult extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 计算批次号 */
    private String batchNo;

    /** 物料ID */
    private Long materialId;

    /** 物料类型：FABRIC/TRIM/PACKAGING */
    private String materialType;

    /** 毛需求（BOM展开后的总需求量） */
    private BigDecimal grossDemand;

    /** 可用库存 */
    private BigDecimal allocatedStock;

    /** 在途数量 */
    private BigDecimal inTransitQuantity;

    /** 净需求 = 毛需求 - 可用库存 - 在途数量 */
    private BigDecimal netDemand;

    /** 建议采购量（考虑MOQ和采购倍数后） */
    private BigDecimal suggestedQuantity;

    /** 单位 */
    private String unit;

    /** 建议供应商ID */
    private Long suggestedSupplierId;

    /** 预估成本 */
    private BigDecimal estimatedCost;

    /** 最早到货日期 */
    private LocalDate earliestDeliveryDate;

    /** 状态：PENDING/APPROVED/CONVERTED/IGNORED */
    private MrpResultStatus status;

    /** 计算快照时间 */
    private LocalDateTime snapshotTime;

    /** 备注 */
    private String remark;
}
