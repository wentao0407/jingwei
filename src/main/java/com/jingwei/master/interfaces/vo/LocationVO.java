package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 库位响应 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class LocationVO {

    private Long id;
    private Long warehouseId;
    private String zoneCode;
    private String rackCode;
    private String rowCode;
    private String binCode;
    private String fullCode;
    private String locationType;
    private Integer capacity;
    private Integer usedCapacity;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
