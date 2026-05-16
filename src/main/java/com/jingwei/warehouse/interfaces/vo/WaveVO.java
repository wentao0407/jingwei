package com.jingwei.warehouse.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class WaveVO {
    private Long id;
    private String waveNo;
    private Long warehouseId;
    private String strategy;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private List<PickListVO> pickLists;
}
