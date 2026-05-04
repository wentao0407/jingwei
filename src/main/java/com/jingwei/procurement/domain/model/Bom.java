package com.jingwei.procurement.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * BOM（物料清单）聚合根
 * <p>
 * 对应数据库表 t_bom。一个款式可有多个版本 BOM，同一 SPU 仅一个 APPROVED 版本。
 * 生产订单关联具体 bom_id，不受后续版本变更影响。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_bom")
public class Bom extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** BOM编码 */
    private String code;

    /** 款式ID（外键→t_md_spu） */
    private Long spuId;

    /** BOM版本号（从1开始递增，区别于乐观锁version） */
    private Integer bomVersion;

    /** 状态：DRAFT/APPROVED/OBSOLETE */
    private BomStatus status;

    /** 生效日期 */
    private LocalDate effectiveFrom;

    /** 失效日期（NULL表示持续有效） */
    private LocalDate effectiveTo;

    /** 审批人ID */
    private Long approvedBy;

    /** 审批时间 */
    private LocalDateTime approvedAt;

    /** 备注 */
    private String remark;

    /** BOM行列表（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private List<BomItem> items = new ArrayList<>();
}
