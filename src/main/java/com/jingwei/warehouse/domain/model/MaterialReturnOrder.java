package com.jingwei.warehouse.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jingwei.common.domain.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@TableName("t_warehouse_material_return")
public class MaterialReturnOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private String returnNo;
    private Long productionOrderId;
    private MaterialReturnStatus status;
    private String remark;

    @TableField(exist = false)
    private List<MaterialReturnLine> lines = new ArrayList<>();
}
