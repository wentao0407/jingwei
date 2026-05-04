package com.jingwei.procurement.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * BOM 行项目 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class BomItemVO {

    private Long id;
    private Long bomId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String materialType;
    private String consumptionType;
    private String consumptionTypeLabel;
    private BigDecimal baseConsumption;
    private Long baseSizeId;
    private String unit;
    private BigDecimal wastageRate;
    private Map<String, Object> sizeConsumptions;
    private Integer sortOrder;
    private String remark;
}
