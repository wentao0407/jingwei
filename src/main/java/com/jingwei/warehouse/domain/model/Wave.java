package com.jingwei.warehouse.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 波次聚合根
 * <p>
 * 对应数据库表 t_warehouse_wave。
 * 波次将多张出库单合并，按策略（客户/物流/库区）生成拣货任务。
 * </p>
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_warehouse_wave")
public class Wave extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 波次编号 */
    private String waveNo;

    /** 仓库ID */
    private Long warehouseId;

    /** 波次策略 */
    private WaveStrategy strategy;

    /** 状态 */
    private WaveStatus status;

    /** 备注 */
    private String remark;

    /** 拣货单列表（非数据库字段） */
    @TableField(exist = false)
    private List<PickList> pickLists = new ArrayList<>();
}
