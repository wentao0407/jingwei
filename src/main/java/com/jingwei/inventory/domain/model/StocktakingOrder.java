package com.jingwei.inventory.domain.model;

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
 * 盘点单聚合根
 * <p>
 * 对应数据库表 t_inventory_stocktaking，管理盘点全生命周期。
 * 盘点期间自动冻结被盘库位，完成后自动解冻。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_inventory_stocktaking")
public class StocktakingOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 盘点单号（编码规则生成，格式 PD-年月日-4位流水号） */
    private String stocktakingNo;

    /** 盘点类型 */
    private StocktakingType stocktakingType;

    /** 盘点模式 */
    private CountMode countMode;

    /** 盘点仓库ID */
    private Long warehouseId;

    /** 盘点库区（可选，循环盘点用） */
    private String zoneCode;

    /** 状态 */
    private StocktakingStatus status;

    /** 计划盘点日期 */
    private LocalDate plannedDate;

    /** 实际开始时间 */
    private LocalDateTime startedAt;

    /** 完成时间 */
    private LocalDateTime completedAt;

    /** 差异审核人ID */
    private Long reviewerId;

    /** 审核时间 */
    private LocalDateTime reviewedAt;

    /** 备注 */
    private String remark;

    /** 盘点行列表（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private List<StocktakingLine> lines = new ArrayList<>();
}
