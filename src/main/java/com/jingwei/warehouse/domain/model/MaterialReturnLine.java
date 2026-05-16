package com.jingwei.warehouse.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@TableName("t_warehouse_material_return_line")
public class MaterialReturnLine extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long returnId;
    private Long materialId;
    private String batchNo;
    private BigDecimal quantity;
    private String unit;
    private String remark;
}
