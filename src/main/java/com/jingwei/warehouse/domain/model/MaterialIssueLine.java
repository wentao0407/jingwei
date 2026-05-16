package com.jingwei.warehouse.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 领料单行
 *
 * @author JingWei
 */
@Getter
@Setter
@TableName("t_warehouse_material_issue_line")
public class MaterialIssueLine extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long issueId;
    private Long materialId;
    private String batchNo;
    private BigDecimal quantity;
    private String unit;
    private String remark;
}
