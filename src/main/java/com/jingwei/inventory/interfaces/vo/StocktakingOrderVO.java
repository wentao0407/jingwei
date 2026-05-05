package com.jingwei.inventory.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 盘点单 VO
 *
 * @author JingWei
 */
@Data
public class StocktakingOrderVO {
    private Long id;
    private String stocktakingNo;
    private String stocktakingType;
    private String stocktakingTypeLabel;
    private String countMode;
    private String countModeLabel;
    private Long warehouseId;
    private String warehouseName;
    private String zoneCode;
    private String status;
    private String statusLabel;
    private String plannedDate;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** 盘点行列表（盲盘模式下 systemQty 为空） */
    private List<StocktakingLineVO> lines;
}
