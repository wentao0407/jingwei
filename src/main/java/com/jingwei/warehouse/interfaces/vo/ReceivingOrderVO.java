package com.jingwei.warehouse.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收货单 VO
 *
 * @author JingWei
 */
@Data
public class ReceivingOrderVO {
    private Long id;
    private String receivingNo;
    private Long asnId;
    private String asnNo;
    private Long warehouseId;
    private String warehouseName;
    private String receivingDate;
    private String status;
    private String statusLabel;
    private Long receiverId;
    private String dockNo;
    private String remark;
    private LocalDateTime createdAt;
    private List<ReceivingLineVO> lines;
}
