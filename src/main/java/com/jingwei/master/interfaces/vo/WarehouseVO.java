package com.jingwei.master.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 仓库响应 VO
 *
 * @author JingWei
 */
@Getter
@Setter
public class WarehouseVO {

    private Long id;
    private String code;
    private String name;
    private String type;
    private String address;
    private Long managerId;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 库位列表（详情查询时填充，列表查询时为 null） */
    private List<LocationVO> locations;
}
