package com.jingwei.procurement.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BOM VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class BomVO {

    private Long id;
    private String code;
    private Long spuId;
    private String spuCode;
    private String spuName;
    private Integer bomVersion;
    private String status;
    private String statusLabel;
    private String effectiveFrom;
    private String effectiveTo;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BomItemVO> items;
}
