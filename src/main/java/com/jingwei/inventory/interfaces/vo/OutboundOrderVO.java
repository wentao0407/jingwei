package com.jingwei.inventory.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库单 VO
 *
 * @author JingWei
 */
@Data
public class OutboundOrderVO {
    private Long id;
    private String outboundNo;
    private String outboundType;
    private String outboundTypeLabel;
    private Long warehouseId;
    private String warehouseName;
    private String status;
    private String statusLabel;
    private String sourceType;
    private Long sourceId;
    private String sourceNo;
    private String outboundDate;
    private String carrier;
    private String trackingNo;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OutboundOrderLineVO> lines;
}
