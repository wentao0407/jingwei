package com.jingwei.warehouse.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 拣货单实体
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_warehouse_pick_list")
public class PickList extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 波次ID */
    private Long waveId;

    /** 拣货单号 */
    private String pickListNo;

    /** 拣货人ID */
    private Long pickerId;

    /** 状态 */
    private PickListStatus status;

    /** 备注 */
    private String remark;

    /** 拣货项列表（非数据库字段） */
    @TableField(exist = false)
    private List<PickItem> items = new ArrayList<>();
}
