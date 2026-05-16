package com.jingwei.warehouse.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PickItemVO {
    private Long id;
    private Long pickListId;
    private Long outboundLineId;
    private Long skuId;
    private Long locationId;
    private String batchNo;
    private BigDecimal plannedQty;
    private BigDecimal actualQty;
    private String status;
    private String remark;
}
