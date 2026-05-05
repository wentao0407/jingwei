package com.jingwei.inventory.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库单 VO
 *
 * @author JingWei
 */
@Data
public class InboundOrderVO {
    private Long id;
    private String inboundNo;
    private String inboundType;
    private String inboundTypeLabel;
    private Long warehouseId;
    private String warehouseName;
    private String status;
    private String statusLabel;
    private String sourceType;
    private Long sourceId;
    private String sourceNo;
    private String inboundDate;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<InboundOrderLineVO> lines;
}
