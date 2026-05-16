package com.jingwei.warehouse.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 领料单聚合根
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_warehouse_material_issue")
public class MaterialIssueOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String issueNo;
    private Long productionOrderId;
    private Long productionLineId;
    private MaterialIssueStatus status;
    private String remark;

    @TableField(exist = false)
    private List<MaterialIssueLine> lines = new ArrayList<>();
}
